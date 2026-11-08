package com.banking.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/** Comando de transferencia Yanki hacia ms-transaction-service. */
@Value
@Builder
public class WalletTransferCommand {

    UUID transactionId;
    UUID sourceWalletId;
    String targetPhoneNumber;
    BigDecimal amount;
    String currency;
    String correlationId;
}
