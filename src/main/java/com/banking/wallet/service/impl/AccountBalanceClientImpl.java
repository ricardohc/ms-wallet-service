package com.banking.wallet.service.impl;

import com.banking.wallet.exception.WalletUnprocessableException;
import com.banking.wallet.service.AccountBalanceClient;
import com.banking.wallet.service.impl.ExternalServicesProperties;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Implementacion de consulta de saldo de cuenta principal. Parte II hace la
 * llamada HTTP con timeout 2s; en esta etapa devuelve 422 si no hay tarjeta
 * vinculada y simula fallback con saldo 0 mientras no exista el cliente HTTP.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountBalanceClientImpl implements AccountBalanceClient {

    private final ExternalServicesProperties properties;

    @Override
    public Mono<BigDecimal> getPrimaryAccountBalance(
            UUID walletId, String linkedDebitCardId, String correlationId) {
        if (linkedDebitCardId == null || linkedDebitCardId.isBlank()) {
            return Mono.error(new WalletUnprocessableException(
                    "Wallet has no linked debit card; primary account balance unavailable"));
        }
        log.info("Querying primary account balance for wallet {} (url={})",
                walletId, properties.getAccountBaseUrl());
        // TODO Parte II: WebClient call con Resilience4j timeout 2s.
        return Mono.just(BigDecimal.ZERO);
    }
}
