package com.banking.wallet.service.impl;

import com.banking.wallet.client.AccountBalanceClient;
import com.banking.wallet.client.AccountValidationClient;
import com.banking.wallet.client.TransactionServiceClient;
import com.banking.wallet.document.WalletDocument;
import com.banking.wallet.dto.WalletCreateRequest;
import com.banking.wallet.dto.WalletResponse;
import com.banking.wallet.dto.WalletStatus;
import com.banking.wallet.dto.WalletTransferRequest;
import com.banking.wallet.dto.WalletUpdateRequest;
import com.banking.wallet.exception.BadRequestException;
import com.banking.wallet.exception.WalletConflictException;
import com.banking.wallet.exception.WalletForbiddenException;
import com.banking.wallet.exception.WalletNotFoundException;
import com.banking.wallet.exception.WalletUnprocessableException;
import com.banking.wallet.event.WalletEvent;
import com.banking.wallet.event.WalletEventPublisher;
import com.banking.wallet.mapper.WalletMapper;
import com.banking.wallet.repository.WalletRepository;
import com.banking.wallet.service.WalletService;
import com.banking.wallet.service.WalletTransferCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Logica de negocio del monedero Yanki.
 * Reglas: unicidad documento/telefono, saldo propio sin tarjeta (fuente de
 * verdad), con tarjeta balance local = 0.00 y fuente de verdad ms-account.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private static final String SUPPORTED_CURRENCY = "PEN";

    private final WalletRepository repository;
    private final WalletMapper mapper;
    private final WalletEventPublisher eventPublisher;
    private final TransactionServiceClient transactionServiceClient;
    private final AccountBalanceClient accountBalanceClient;
    private final AccountValidationClient accountValidationClient;

    @Override
    public Mono<WalletResponse> create(WalletCreateRequest request, UUID transactionId) {
        return repository.existsByDocumentTypeAndDocumentNumber(
                        request.getDocumentType().getValue(), request.getDocumentNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new WalletConflictException(
                        "Wallet already exists for document"))
                        : repository.existsByPhoneNumber(request.getPhoneNumber()))
                .flatMap(phoneExists -> phoneExists
                        ? Mono.error(new WalletConflictException(
                        "Wallet already exists for phone number"))
                        : persistNewWallet(request, transactionId))
                .onErrorResume(DuplicateKeyException.class,
                        e -> Mono.error(new WalletConflictException(
                                "Wallet already exists for document or phone")));
    }

    private Mono<WalletResponse> persistNewWallet(WalletCreateRequest request, UUID transactionId) {
        WalletDocument doc = mapper.newWallet(
                request.getDocumentType().getValue(),
                request.getDocumentNumber(),
                request.getPhoneNumber(),
                request.getImei(),
                request.getEmail());
        return repository.save(doc)
                .flatMap(saved -> publishEvent("WalletCreated", saved)
                        .thenReturn(mapper.toResponse(saved)))
                .doOnSuccess(r -> log.info("Wallet created txId={}", transactionId));
    }

    @Override
    public Mono<WalletResponse> findById(UUID walletId) {
        return repository.findByWalletUuid(walletId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found")))
                .map(mapper::toResponse);
    }

    @Override
    public Mono<WalletResponse> update(UUID walletId, WalletUpdateRequest request,
                                       UUID transactionId) {
        return repository.findByWalletUuid(walletId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found")))
                .flatMap(doc -> applyUpdate(doc, request))
                .flatMap(repository::save)
                .flatMap(saved -> publishEvent("WalletUpdated", saved)
                        .thenReturn(mapper.toResponse(saved)));
    }

    private Mono<WalletDocument> applyUpdate(WalletDocument doc, WalletUpdateRequest request) {
        if (request.getLinkedDebitCardId() != null) {
            return accountValidationClient.validateLinkableAccount(request.getLinkedDebitCardId())
                    .thenReturn(withLinkedCard(doc, request.getLinkedDebitCardId()));
        }
        if (request.getStatus() != null) {
            doc.setStatus(request.getStatus().getValue());
        }
        return Mono.just(doc);
    }

    private WalletDocument withLinkedCard(WalletDocument doc, UUID cardId) {
        doc.setLinkedDebitCardId(cardId);
        doc.setBalance(new BigDecimal("0.00"));
        return doc;
    }

    @Override
    public Mono<Void> deactivate(UUID walletId, UUID transactionId) {
        return repository.findByWalletUuid(walletId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found")))
                .flatMap(doc -> {
                    doc.setStatus(WalletStatus.INACTIVE.getValue());
                    return repository.save(doc)
                            .then(publishEvent("WalletDeactivated", doc));
                });
    }

    @Override
    public Mono<UUID> transfer(WalletTransferRequest request, String sourcePhoneNumber,
                               UUID transactionId) {
        return Mono.defer(() -> {
            // --- Validacion 1: moneda soportada -> 400 ---
            if (request.getCurrency() == null
                    || !SUPPORTED_CURRENCY.equalsIgnoreCase(request.getCurrency())) {
                return Mono.error(new BadRequestException(
                        "Unsupported currency. Only PEN is allowed"));
            }

            // --- Validacion 2: source no nulo -> 400 ---
            if (sourcePhoneNumber == null || sourcePhoneNumber.isBlank()) {
                return Mono.error(new BadRequestException(
                        "Source phone number is required"));
            }

            // --- Validacion 3: source != target -> 400 ---
            if (sourcePhoneNumber.equals(request.getTargetPhoneNumber())) {
                return Mono.error(new BadRequestException(
                        "Source phone number and target phone number cannot be the same"));
            }

            BigDecimal amount = new BigDecimal(request.getAmount());

            // --- Buscamos el wallet origen ---
            return repository.findByPhoneNumber(sourcePhoneNumber)
                    .switchIfEmpty(Mono.error(new WalletNotFoundException(
                            "Source wallet not found for phone number")))
                    .flatMap(source -> {
                        if (!WalletStatus.ACTIVE.getValue().equals(source.getStatus())) {
                            return Mono.error(new WalletForbiddenException(
                                    "Wallet is not active"));
                        }

                        // --- Validacion 4: fondos suficientes -> 422 ---
                        Mono<BigDecimal> balanceMono;
                        if (source.getLinkedDebitCardId() != null) {
                            balanceMono = accountBalanceClient
                                    .getPrimaryAccountBalance(source.getWalletUuid(),
                                            source.getLinkedDebitCardId().toString(), null);
                        } else {
                            balanceMono = Mono.just(source.getBalance() == null
                                    ? BigDecimal.ZERO : source.getBalance());
                        }

                        return balanceMono.flatMap(balance -> {
                            if (balance.compareTo(amount) < 0) {
                                return Mono.error(new WalletUnprocessableException(
                                        "Insufficient funds in source wallet"));
                            }
                            return delegateTransfer(source, request, transactionId);
                        });
                    });
        });
    }

    private Mono<UUID> delegateTransfer(WalletDocument source, WalletTransferRequest request,
                                        UUID transactionId) {
        WalletTransferCommand command = WalletTransferCommand.builder()
                .transactionId(transactionId)
                .sourceWalletId(source.getWalletUuid())
                .targetPhoneNumber(request.getTargetPhoneNumber())
                .amount(new BigDecimal(request.getAmount()))
                .currency(request.getCurrency())
                .build();
        return transactionServiceClient.createTransfer(command);
    }

    @Override
    public Mono<com.banking.wallet.dto.BalanceResponse> findBalance(
            String authenticatedPhoneNumber) {
        return repository.findByPhoneNumber(authenticatedPhoneNumber)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(
                        "Wallet not found for phone number")))
                .flatMap(doc -> {
                    if (doc.getLinkedDebitCardId() != null) {
                        return accountBalanceClient
                                .getPrimaryAccountBalance(doc.getWalletUuid(),
                                        doc.getLinkedDebitCardId().toString(), null)
                                .map(balance -> toBalance(balance, doc.getCurrency()));
                    }
                    return Mono.just(toBalance(doc.getBalance(), doc.getCurrency()));
                });
    }

    private com.banking.wallet.dto.BalanceResponse toBalance(BigDecimal balance, String currency) {
        com.banking.wallet.dto.BalanceResponse r = new com.banking.wallet.dto.BalanceResponse();
        r.setBalance(balance.toPlainString());
        r.setCurrency(currency);
        return r;
    }

    private Mono<Void> publishEvent(String eventType, WalletDocument doc) {
        WalletEvent event = WalletEvent.builder()
                .eventType(eventType)
                .walletId(doc.getWalletUuid())
                .phoneNumber(doc.getPhoneNumber())
                .occurredAt(Instant.now())
                .build();
        return eventPublisher.publish(event).onErrorResume(e -> {
            log.warn("Failed to publish wallet event {}: {}", eventType, e.getMessage());
            return Mono.empty();
        });
    }
}