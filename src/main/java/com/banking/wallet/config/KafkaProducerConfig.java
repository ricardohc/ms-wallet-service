package com.banking.wallet.config;

import com.banking.wallet.event.WalletEvent;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

/** Configuracion del productor Kafka reactivo para eventos de wallet. */
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, WalletEvent> reactiveKafkaProducerTemplate(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        SenderOptions<String, WalletEvent> options = SenderOptions
                .<String, WalletEvent>create(Map.of(
                        "bootstrap.servers", bootstrapServers));
        return new ReactiveKafkaProducerTemplate<>(options);
    }
}
