package com.banking.wallet.client;

import java.math.BigDecimal;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Cliente hacia ms-account-service para consultar el saldo de la cuenta
 * principal cuando el monedero tiene tarjeta de debito vinculada.
 */
public interface AccountBalanceClient {

    Mono<BigDecimal> getPrimaryAccountBalance(UUID walletId, String linkedDebitCardId,
                                              String correlationId);
}