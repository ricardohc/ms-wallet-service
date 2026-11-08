package com.banking.wallet.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Productor de eventos de dominio hacia banking.wallet.events.v1.
 * Los eventos de movimiento se formalizan en el catalogo antes de publicarse.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventPublisher {

    private static final String TOPIC = "banking.wallet.events.v1";

    private final ReactiveKafkaProducerTemplate<String, WalletEvent> kafkaTemplate;

    public Mono<Void> publish(WalletEvent event) {
        return kafkaTemplate.send(TOPIC, event.getWalletId().toString(), event)
                .doOnSuccess(ignored -> log.info("Wallet event published: {}", event.getEventType()))
                .then();
    }
}
