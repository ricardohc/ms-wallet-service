package com.banking.wallet.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletEvent {

    private String eventId;
    private String eventType;
    private BigDecimal occurredAt;
    private String correlationId;
    private WalletPayload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletPayload {
        private UUID id;
        private String documentType;
        private String documentNumber;
        private String phoneNumber;
        private String imei;
        private String email;
        private String status;
        private UUID linkedDebitCardId;
        private String balance;
        private String currency;
        private Instant createdAt;
    }
}