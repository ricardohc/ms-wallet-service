package com.banking.wallet.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventPublisher {

    public static final String TOPIC = "banking.wallet.events.v1";

    private final ReactiveKafkaProducerTemplate<String, WalletEvent> kafkaTemplate;

    public Mono<Void> publish(WalletEvent event) {
        String key = event.getPayload() != null && event.getPayload().getId() != null
                ? event.getPayload().getId().toString()
                : event.getEventId();
        return kafkaTemplate.send(TOPIC, key, event)
                .doOnSuccess(ignored -> log.info(
                        "Wallet event published topic={} eventType={} key={}",
                        TOPIC, event.getEventType(), key))
                .onErrorResume(e -> {
                    log.error("Failed to publish wallet event {}: {}",
                            event.getEventType(), e.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}