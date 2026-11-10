package com.banking.wallet.service.impl;

import com.banking.wallet.document.AccountReferenceDocument;
import com.banking.wallet.document.WalletDocument;
import com.banking.wallet.dto.WalletCreateRequest;
import com.banking.wallet.dto.WalletResponse;
import com.banking.wallet.dto.WalletStatus;
import com.banking.wallet.dto.WalletUpdateRequest;
import com.banking.wallet.event.WalletEvent;
import com.banking.wallet.event.WalletEventPublisher;
import com.banking.wallet.exception.WalletConflictException;
import com.banking.wallet.exception.WalletNotFoundException;
import com.banking.wallet.exception.WalletUnprocessableException;
import com.banking.wallet.mapper.WalletMapper;
import com.banking.wallet.repository.AccountReferenceRepository;
import com.banking.wallet.repository.WalletRepository;
import com.banking.wallet.service.WalletService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    /** Tipos de cuenta permitidos para vincular una tarjeta débito al wallet. */
    private static final Set<String> ALLOWED_LINK_ACCOUNT_TYPES =
            Set.of("SAVINGS", "CHECKED");

    private final WalletRepository repository;
    private final AccountReferenceRepository accountReferenceRepository;
    private final WalletMapper mapper;
    private final WalletEventPublisher eventPublisher;

    @Override
    public Mono<WalletResponse> create(WalletCreateRequest request, UUID transactionId,
                                       String correlationId) {
        return repository.existsByDocumentTypeAndDocumentNumber(
                        request.getDocumentType().getValue(), request.getDocumentNumber())
                .flatMap(exists -> exists
                        ? Mono.<Boolean>error(new WalletConflictException(
                        "Wallet already exists for document"))
                        : repository.existsByPhoneNumber(request.getPhoneNumber()))
                .flatMap(phoneExists -> phoneExists
                        ? Mono.<WalletResponse>error(new WalletConflictException(
                        "Wallet already exists for phone number"))
                        : persistNewWallet(request, transactionId, correlationId))
                .onErrorResume(DuplicateKeyException.class,
                        e -> Mono.error(new WalletConflictException(
                                "Wallet already exists for document or phone")));
    }

    private Mono<WalletResponse> persistNewWallet(WalletCreateRequest request,
                                                  UUID transactionId,
                                                  String correlationId) {
        WalletDocument doc = mapper.newWallet(
                request.getDocumentType().getValue(),
                request.getDocumentNumber(),
                request.getPhoneNumber(),
                request.getImei(),
                request.getEmail());
        return repository.save(doc)
                .flatMap(saved -> publishEvent("i", saved, correlationId)
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
                                       UUID transactionId, String correlationId) {
        return repository.findByWalletUuid(walletId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found")))
                .flatMap(doc -> applyUpdate(doc, request))
                .flatMap(repository::save)
                .flatMap(saved -> publishEvent("u", saved, correlationId)
                        .thenReturn(mapper.toResponse(saved)));
    }

    private Mono<WalletDocument> applyUpdate(WalletDocument doc, WalletUpdateRequest request) {
        // Validación de negocio: solo SAVINGS o CHECKED pueden vincularse.
        Mono<Void> linkValidation = Mono.empty();
        if (request.getLinkedDebitCardId() != null) {
            linkValidation = validateLinkedAccountType(request.getLinkedDebitCardId());
        }

        return linkValidation.then(Mono.defer(() -> {
            if (request.getLinkedDebitCardId() != null) {
                doc.setLinkedDebitCardId(request.getLinkedDebitCardId());
                doc.setBalance(new BigDecimal("0.00"));
            }
            if (request.getStatus() != null) {
                doc.setStatus(request.getStatus().getValue());
            }
            return Mono.just(doc);
        }));
    }

    private Mono<Void> validateLinkedAccountType(UUID linkedDebitCardId) {
        String accountId = linkedDebitCardId.toString();
        return accountReferenceRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new WalletUnprocessableException(
                        "Linked account not found in account_reference: " + accountId)))
                .flatMap(this::assertAllowedAccountType)
                .then();
    }

    private Mono<AccountReferenceDocument> assertAllowedAccountType(AccountReferenceDocument account) {
        String type = account.getAccountType();
        if (type == null || !ALLOWED_LINK_ACCOUNT_TYPES.contains(type)) {
            log.warn("Rejecting link to account {} with accountType={}", account.getId(), type);
            return Mono.error(new WalletUnprocessableException(
                    "Account " + account.getId() + " has accountType=" + type
                            + " not allowed for debit card linking. Allowed: SAVINGS, CHECKED"));
        }
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            return Mono.error(new WalletUnprocessableException(
                    "Linked account " + account.getId() + " is not ACTIVE"));
        }
        return Mono.just(account);
    }

    @Override
    public Mono<Void> deactivate(UUID walletId, UUID transactionId, String correlationId) {
        return repository.findByWalletUuid(walletId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found")))
                .flatMap(doc -> {
                    doc.setStatus(WalletStatus.INACTIVE.getValue());
                    return repository.save(doc)
                            .flatMap(saved -> publishEvent("d", saved, correlationId));
                });
    }

    private Mono<Void> publishEvent(String eventType, WalletDocument doc, String correlationId) {
        WalletEvent event = WalletEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .occurredAt(nowEpochSeconds())
                .correlationId(correlationId)
                .payload(mapper.toEventPayload(doc))
                .build();
        return eventPublisher.publish(event).onErrorResume(e -> {
            log.warn("Failed to publish wallet event {}: {}", eventType, e.getMessage());
            return Mono.empty();
        });
    }

    private BigDecimal nowEpochSeconds() {
        Instant now = Instant.now();
        return BigDecimal.valueOf(now.getEpochSecond())
                .add(BigDecimal.valueOf(now.getNano(), 9))
                .setScale(9, RoundingMode.HALF_UP);
    }
}