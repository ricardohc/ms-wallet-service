package com.banking.wallet.client.impl;

import com.banking.wallet.client.AccountValidationClient;
import com.banking.wallet.config.ExternalServicesProperties;
import com.banking.wallet.exception.WalletConflictException;
import com.banking.wallet.exception.WalletUnprocessableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Implementacion del cliente de validacion de cuentas contra ms-account-service.
 * Verifica que el accountType no sea FIXED_TERM.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountValidationClientImpl implements AccountValidationClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final ExternalServicesProperties properties;

    @Override
    public Mono<Void> validateLinkableAccount(UUID accountId) {
        String url = properties.getAccountBaseUrl() + "/api/v1/accounts/" + accountId;
        log.debug("Validating linked account {} against ms-account-service", accountId);
        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Accept", "application/json")
                // Headers obligatorios exigidos por ms-account-service (OpenAPI)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Channel-ID", "MS-WALLET-SERVICE")
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(body -> {
                    try {
                        JsonNode node = objectMapper.readTree(body);
                        String accountType = node.path("accountType").asText(null);
                        if ("FIXED_TERM".equals(accountType)) {
                            return Mono.error(new WalletConflictException(
                                    "Fixed-term accounts are not compatible with wallet linking"));
                        }
                        return Mono.empty();
                    } catch (Exception ex) {
                        log.error("Failed to parse account response: {}", ex.getMessage());
                        return Mono.error(new WalletUnprocessableException(
                                "Unable to validate linked account"));
                    }
                })
                .onErrorResume(WebClientResponseException.NotFound.class,
                        ex -> Mono.error(new WalletUnprocessableException(
                                "Linked account not found: " + accountId)))
                .onErrorResume(ex -> {
                    if (ex instanceof WalletConflictException
                            || ex instanceof WalletUnprocessableException) {
                        return Mono.error(ex);
                    }
                    log.error("Failed to validate account {}: {}", accountId, ex.getMessage());
                    return Mono.error(new WalletUnprocessableException(
                            "Unable to validate linked account: " + accountId));
                })
                .then();
    }
}