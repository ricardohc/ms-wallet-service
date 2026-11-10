package com.banking.wallet.config;

import com.banking.wallet.event.WalletEvent;
import java.util.Map;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, WalletEvent> reactiveKafkaProducerTemplate(
            KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties(null);
        SenderOptions<String, WalletEvent> options = SenderOptions.create(props);
        return new ReactiveKafkaProducerTemplate<>(options);
    }
}