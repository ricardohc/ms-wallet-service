package com.banking.wallet.client;

import com.banking.wallet.exception.WalletConflictException;
import com.banking.wallet.exception.WalletUnprocessableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Cliente hacia ms-account-service para validar cuentas antes de vincularlas
 * a un monedero. Verifica que el accountType no sea FIXED_TERM.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountValidationClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${external.services.account-base-url:http://localhost:8082}")
    private String accountBaseUrl;

    /**
     * Valida que la cuenta exista y no sea FIXED_TERM.
     * Lanza WalletConflictException (409) si es FIXED_TERM.
     * Lanza WalletUnprocessableException (422) si no existe o hay error.
     */
    public Mono<Void> validateLinkableAccount(UUID accountId) {
        String url = accountBaseUrl + "/api/v1/accounts/" + accountId;
        log.debug("Validating linked account {} against ms-account-service", accountId);
        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Accept", "application/json")
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