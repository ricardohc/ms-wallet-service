package com.banking.wallet.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.banking.wallet.client.AccountBalanceClient;
import com.banking.wallet.client.AccountValidationClient;
import com.banking.wallet.client.TransactionServiceClient;
import com.banking.wallet.document.WalletDocument;
import com.banking.wallet.dto.WalletCreateRequest;
import com.banking.wallet.dto.WalletTransferRequest;
import com.banking.wallet.exception.WalletConflictException;
import com.banking.wallet.exception.WalletForbiddenException;
import com.banking.wallet.exception.WalletNotFoundException;
import com.banking.wallet.event.WalletEventPublisher;
import com.banking.wallet.mapper.WalletMapper;
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

/** Pruebas unitarias de reglas de negocio del monedero (reactor-test). */
@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository repository;

    @Mock
    private WalletEventPublisher eventPublisher;

    @Mock
    private TransactionServiceClient transactionServiceClient;

    @Mock
    private AccountBalanceClient accountBalanceClient;

    @Mock
    private AccountValidationClient accountValidationClient;

    private WalletServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WalletServiceImpl(repository, new WalletMapper(),
                eventPublisher, transactionServiceClient, accountBalanceClient,
                accountValidationClient);
    }

    private WalletDocument doc(String status) {
        return WalletDocument.builder()
                .walletUuid(UUID.randomUUID())
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

    @Test
    void createValidWalletReturnsResponse() {
        when(repository.existsByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(repository.existsByPhoneNumber(anyString())).thenReturn(Mono.just(false));
        when(repository.save(any(WalletDocument.class)))
                .thenReturn(Mono.just(doc("ACTIVE")));
        when(eventPublisher.publish(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.create(createRequest(), UUID.randomUUID()))
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

        StepVerifier.create(service.create(createRequest(), UUID.randomUUID()))
                .expectError(WalletConflictException.class)
                .verify();
    }

    @Test
    void createWithDuplicatedPhoneReturns409() {
        when(repository.existsByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(repository.existsByPhoneNumber(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(service.create(createRequest(), UUID.randomUUID()))
                .expectError(WalletConflictException.class)
                .verify();
    }

    @Test
    void transferWithoutJwtReturnsForbidden() {
        WalletTransferRequest req = new WalletTransferRequest();
        req.setTargetPhoneNumber("987654322");
        req.setAmount("10.00");
        req.setCurrency("PEN");

        StepVerifier.create(service.transfer(req, null, UUID.randomUUID()))
                .expectError(WalletForbiddenException.class)
                .verify();
    }

    @Test
    void transferWithPhoneNotOwnedReturnsNotFound() {
        WalletTransferRequest req = new WalletTransferRequest();
        req.setTargetPhoneNumber("987654322");
        req.setAmount("10.00");
        req.setCurrency("PEN");
        when(repository.findByPhoneNumber(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(service.transfer(req, "111111111", UUID.randomUUID()))
                .expectError(WalletNotFoundException.class)
                .verify();
    }

    @Test
    void transferWithInactiveWalletReturnsForbidden() {
        WalletTransferRequest req = new WalletTransferRequest();
        req.setTargetPhoneNumber("987654322");
        req.setAmount("10.00");
        req.setCurrency("PEN");
        when(repository.findByPhoneNumber(anyString()))
                .thenReturn(Mono.just(doc("INACTIVE")));

        StepVerifier.create(service.transfer(req, "987654321", UUID.randomUUID()))
                .expectError(WalletForbiddenException.class)
                .verify();
    }

    @Test
    void transferActiveWalletDelegatesToTransactionService() {
        WalletTransferRequest req = new WalletTransferRequest();
        req.setTargetPhoneNumber("987654322");
        req.setAmount("10.00");
        req.setCurrency("PEN");
        when(repository.findByPhoneNumber(anyString()))
                .thenReturn(Mono.just(doc("ACTIVE")));
        UUID operationId = UUID.randomUUID();
        when(transactionServiceClient.createTransfer(any()))
                .thenReturn(Mono.just(operationId));

        StepVerifier.create(service.transfer(req, "987654321", UUID.randomUUID()))
                .expectNext(operationId)
                .verifyComplete();
    }

    @Test
    void balanceWithoutCardUsesOwnBalance() {
        when(repository.findByPhoneNumber(anyString()))
                .thenReturn(Mono.just(doc("ACTIVE")));

        StepVerifier.create(service.findBalance("987654321"))
                .assertNext(r -> {
                    org.junit.jupiter.api.Assertions.assertEquals("50.00",
                            r.getBalance());
                    org.junit.jupiter.api.Assertions.assertEquals("PEN",
                            r.getCurrency());
                })
                .verifyComplete();
    }

    @Test
    void balanceWithCardQueriesAccountService() {
        WalletDocument withCard = doc("ACTIVE");
        withCard.setLinkedDebitCardId(UUID.randomUUID());
        when(repository.findByPhoneNumber(anyString()))
                .thenReturn(Mono.just(withCard));
        when(accountBalanceClient.getPrimaryAccountBalance(any(), anyString(), any()))
                .thenReturn(Mono.just(new BigDecimal("100.50")));

        StepVerifier.create(service.findBalance("987654321"))
                .assertNext(r -> org.junit.jupiter.api.Assertions.assertEquals("100.50",
                        r.getBalance()))
                .verifyComplete();
    }
}