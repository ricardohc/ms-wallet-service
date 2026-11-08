package com.banking.wallet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuracion de clientes HTTP externos (account, card, transaction). */
@Data
@Configuration
@ConfigurationProperties(prefix = "external.services")
public class ExternalServicesProperties {

    private String accountBaseUrl = "http://localhost:8082";
    private String cardBaseUrl = "http://localhost:8084";
    private String transactionBaseUrl = "http://localhost:8083";
}