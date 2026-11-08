package com.banking.wallet.client;

import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Cliente hacia ms-account-service para validar cuentas antes de vincularlas
 * a un monedero. Verifica que el accountType no sea FIXED_TERM.
 */
public interface AccountValidationClient {

    Mono<Void> validateLinkableAccount(UUID accountId);
}