package com.banking.wallet.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    public static final String WALLET_EVENTS_TOPIC = "banking.wallet.events.v1";
    public static final String ACCOUNT_EVENTS_TOPIC = "banking.account.events.v1";

    @Bean
    public NewTopic walletEventsTopic() {
        return TopicBuilder.name(WALLET_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic accountEventsTopic() {
        return TopicBuilder.name(ACCOUNT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}