package com.banking.wallet.service;

import java.util.UUID;
import reactor.core.publisher.Mono;

/** Cliente hacia ms-transaction-service para orquestar la transferencia Yanki. */
public interface TransactionServiceClient {

    Mono<UUID> createTransfer(WalletTransferCommand command);
}
