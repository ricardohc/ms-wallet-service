package com.banking.wallet.event.consumer;

import com.banking.wallet.document.AccountReferenceDocument;
import com.banking.wallet.repository.AccountReferenceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventListener {

    public static final String TOPIC = "banking.account.events.v1";

    private final AccountReferenceRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC, groupId = "wallet-service-group")
    public void listen(@Payload String message) {
        log.info("Account event received: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText();
            JsonNode payload = root.path("payload");

            if (payload.isMissingNode() || payload.path("id").isMissingNode()) {
                log.warn("Invalid account event (missing payload/id): {}", message);
                return;
            }

            String accountId = payload.path("id").asText();
            Instant occurredAt = parseOccurredAt(root.path("occurredAt"));

            switch (eventType) {
                case "i" -> upsert(accountId, payload, occurredAt, true)
                        .subscribe(
                                saved -> log.info("Account reference inserted: {}", accountId),
                                err -> log.error("Error inserting account reference {}", accountId, err));
                case "u" -> upsert(accountId, payload, occurredAt, false)
                        .subscribe(
                                saved -> log.info("Account reference updated: {}", accountId),
                                err -> log.error("Error updating account reference {}", accountId, err));
                case "d" -> deactivate(accountId)
                        .subscribe(
                                saved -> log.info("Account reference deactivated: {}", accountId),
                                err -> log.error("Error deactivating account reference {}", accountId, err));
                default -> log.debug("Unhandled account eventType={}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing account event: {}", message, e);
        }
    }

    private Mono<AccountReferenceDocument> upsert(
            String accountId, JsonNode payload, Instant occurredAt, boolean isInsert) {
        return repository.findById(accountId)
                .flatMap(existing -> {
                    applyPayload(existing, payload, occurredAt);
                    return repository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    AccountReferenceDocument doc = AccountReferenceDocument.builder()
                            .id(accountId)
                            .occurredAt(occurredAt)
                            .build();
                    applyPayload(doc, payload, occurredAt);
                    if (doc.getStatus() == null) {
                        doc.setStatus("ACTIVE");
                    }
                    if (doc.getBalance() == null) {
                        doc.setBalance(new BigDecimal("0.00"));
                    }
                    log.info("Creating missing account reference {} (isInsert={})", accountId, isInsert);
                    return repository.save(doc);
                }));
    }

    private Mono<AccountReferenceDocument> deactivate(String accountId) {
        return repository.findById(accountId)
                .flatMap(doc -> {
                    doc.setStatus("INACTIVE");
                    return repository.save(doc);
                });
    }

    private void applyPayload(AccountReferenceDocument doc, JsonNode payload, Instant occurredAt) {
        String accountNumber = textOrNull(payload, "accountNumber");
        String customerId = textOrNull(payload, "customerId");
        String accountType = textOrNull(payload, "accountType");
        String profile = textOrNull(payload, "profile");
        String status = textOrNull(payload, "status");
        String balance = textOrNull(payload, "balance");
        String currency = textOrNull(payload, "currency");
        String eventCreatedAt = textOrNull(payload, "createdAt");

        if (accountNumber != null) doc.setAccountNumber(accountNumber);
        if (customerId != null) doc.setCustomerId(UUID.fromString(customerId));
        if (accountType != null) doc.setAccountType(accountType);
        if (profile != null) doc.setProfile(profile);
        if (status != null) doc.setStatus(status);
        if (balance != null) doc.setBalance(new BigDecimal(balance));
        if (currency != null) doc.setCurrency(currency);
        if (eventCreatedAt != null) {
            try {
                doc.setEventCreatedAt(Instant.parse(eventCreatedAt));
            } catch (Exception e) {
                log.warn("Invalid createdAt in account event: {}", eventCreatedAt);
            }
        }
        if (occurredAt != null) doc.setOccurredAt(occurredAt);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private Instant parseOccurredAt(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        try {
            if (node.isNumber()) {
                // epoch en segundos con fracción (ej: 1794217851.270925100)
                BigDecimal epochSeconds = node.decimalValue();
                long seconds = epochSeconds.longValue();
                long nanos = epochSeconds.subtract(BigDecimal.valueOf(seconds))
                        .movePointRight(9)
                        .longValue();
                return Instant.ofEpochSecond(seconds, nanos);
            }
            return Instant.parse(node.asText());
        } catch (Exception e) {
            log.warn("Invalid occurredAt value: {}", node);
            return null;
        }
    }
}