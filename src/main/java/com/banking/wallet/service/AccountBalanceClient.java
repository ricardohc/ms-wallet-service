package com.banking.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Cliente hacia ms-account-service para consultar el saldo de la cuenta
 * principal cuando el monedero tiene tarjeta de debito vinculada.
 * Parte II: API con timeout 2s (Resilience4j). Parte III: proyeccion por eventos.
 */
public interface AccountBalanceClient {

    Mono<BigDecimal> getPrimaryAccountBalance(UUID walletId, String linkedDebitCardId, String correlationId);
}
