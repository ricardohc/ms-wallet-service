package com.banking.wallet.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de dominio del monedero (topico banking.wallet.events.v1).
 * Solo se publica si el catalogo Kafka lo formaliza.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletEvent {

    private String eventType;
    private UUID walletId;
    private String phoneNumber;
    private Instant occurredAt;
}
