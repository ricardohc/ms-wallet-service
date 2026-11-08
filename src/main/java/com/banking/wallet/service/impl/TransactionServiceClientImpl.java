package com.banking.wallet.service.impl;

import com.banking.wallet.service.TransactionServiceClient;
import com.banking.wallet.service.WalletTransferCommand;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Cliente hacia ms-transaction-service. Parte II: llamada HTTP con timeout 2s.
 * En esta etapa se simula la aceptacion con un operationId generado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceClientImpl implements TransactionServiceClient {

    @Override
    public Mono<UUID> createTransfer(WalletTransferCommand command) {
        log.info("Delegating Yanki transfer to transaction service, txId={}",
                command.getTransactionId());
        // TODO Parte II: WebClient POST a transaction service con Resilience4j.
        return Mono.just(command.getTransactionId());
    }
}
