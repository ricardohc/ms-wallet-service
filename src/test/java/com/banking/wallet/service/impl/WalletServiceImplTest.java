package com.banking.wallet.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.banking.wallet.document.AccountReferenceDocument;
import com.banking.wallet.document.WalletDocument;
import com.banking.wallet.dto.WalletCreateRequest;
import com.banking.wallet.dto.WalletStatus;
import com.banking.wallet.dto.WalletUpdateRequest;
import com.banking.wallet.event.WalletEventPublisher;
import com.banking.wallet.exception.WalletConflictException;
import com.banking.wallet.exception.WalletUnprocessableException;
import com.banking.wallet.mapper.WalletMapper;
import com.banking.wallet.repository.AccountReferenceRepository;
import com.banking.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock private WalletRepository repository;
    @Mock private AccountReferenceRepository accountReferenceRepository;
    @Mock private WalletEventPublisher eventPublisher;

    private WalletServiceImpl service;

    private final UUID walletUuid = UUID.randomUUID();
    private final UUID cardUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new WalletServiceImpl(repository, accountReferenceRepository,
                new WalletMapper(), eventPublisher);
    }

    private WalletDocument doc(String status) {
        return WalletDocument.builder()
                .walletUuid(walletUuid)
                .documentType("DNI")
                .documentNumber("12345678")
                .phoneNumber("987654321")
                .email("user@example.com")
                .status(status)
                .balance(new BigDecimal("50.00"))
                .currency("PEN")
                .build();
    }

    private WalletCreateRequest createRequest() {
        WalletCreateRequest req = new WalletCreateRequest();
        req.setDocumentType(com.banking.wallet.dto.DocumentType.DNI);
        req.setDocumentNumber("12345678");
        req.setPhoneNumber("987654321");
        req.setEmail("user@example.com");
        return req;
    }

    private AccountReferenceDocument account(String type, String status) {
        return AccountReferenceDocument.builder()
                .id(cardUuid.toString())
                .accountType(type)
                .status(status)
                .build();
    }

    @Test
    void createValidWalletReturnsResponse() {
        when(repository.existsByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(repository.existsByPhoneNumber(anyString())).thenReturn(Mono.just(false));
        when(repository.save(any(WalletDocument.class)))
                .thenReturn(Mono.just(doc("ACTIVE")));
        when(eventPublisher.publish(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.create(createRequest(), UUID.randomUUID(), "corr-1"))
                .assertNext(r -> {
                    org.junit.jupiter.api.Assertions.assertEquals("987654321",
                            r.getPhoneNumber());
                    org.junit.jupiter.api.Assertions.assertEquals("ACTIVE",
                            r.getStatus().getValue());
                })
                .verifyComplete();
    }

    @Test
    void createWithDuplicatedDocumentReturns409() {
        when(repository.existsByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.create(createRequest(), UUID.randomUUID(), "corr-1"))
                .expectError(WalletConflictException.class)
                .verify();
    }

    @Test
    void createWithDuplicatedPhoneReturns409() {
        when(repository.existsByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(repository.existsByPhoneNumber(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(service.create(createRequest(), UUID.randomUUID(), "corr-1"))
                .expectError(WalletConflictException.class)
                .verify();
    }

    @Test
    void linkSavingsAccountIsAccepted() {
        WalletDocument existing = doc("ACTIVE");
        when(repository.findByWalletUuid(walletUuid)).thenReturn(Mono.just(existing));
        when(accountReferenceRepository.findById(cardUuid.toString()))
                .thenReturn(Mono.just(account("SAVINGS", "ACTIVE")));
        when(repository.save(any(WalletDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(eventPublisher.publish(any())).thenReturn(Mono.empty());

        WalletUpdateRequest req = new WalletUpdateRequest();
        req.setLinkedDebitCardId(cardUuid);
        req.setStatus(WalletStatus.ACTIVE);

        StepVerifier.create(service.update(walletUuid, req, UUID.randomUUID(), "corr-1"))
                .assertNext(r -> org.junit.jupiter.api.Assertions.assertEquals(
                        cardUuid, r.getLinkedDebitCardId()))
                .verifyComplete();
    }

    @Test
    void linkFixTermAccountIsRejected() {
        WalletDocument existing = doc("ACTIVE");
        when(repository.findByWalletUuid(walletUuid)).thenReturn(Mono.just(existing));
        when(accountReferenceRepository.findById(cardUuid.toString()))
                .thenReturn(Mono.just(account("FIX_TERM", "ACTIVE")));

        WalletUpdateRequest req = new WalletUpdateRequest();
        req.setLinkedDebitCardId(cardUuid);

        StepVerifier.create(service.update(walletUuid, req, UUID.randomUUID(), "corr-1"))
                .expectError(WalletUnprocessableException.class)
                .verify();
    }

    @Test
    void linkUnknownAccountIsRejected() {
        WalletDocument existing = doc("ACTIVE");
        when(repository.findByWalletUuid(walletUuid)).thenReturn(Mono.just(existing));
        when(accountReferenceRepository.findById(cardUuid.toString()))
                .thenReturn(Mono.empty());

        WalletUpdateRequest req = new WalletUpdateRequest();
        req.setLinkedDebitCardId(cardUuid);

        StepVerifier.create(service.update(walletUuid, req, UUID.randomUUID(), "corr-1"))
                .expectError(WalletUnprocessableException.class)
                .verify();
    }
}