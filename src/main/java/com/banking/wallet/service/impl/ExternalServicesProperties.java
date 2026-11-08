package com.banking.wallet.service.impl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuracion de clientes HTTP externos (account, card, transaction). */
@Data
@Configuration
@ConfigurationProperties(prefix = "external.services")
public class ExternalServicesProperties {

    private String accountBaseUrl = "http://ms-account-service";
    private String cardBaseUrl = "http://ms-card-service";
    private String transactionBaseUrl = "http://ms-transaction-service";
}
