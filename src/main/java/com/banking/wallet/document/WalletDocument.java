package com.banking.wallet.document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Monedero Yanki. Persistencia propia del servicio (base banking_wallet,
 * coleccion wallets). Separado de los DTOs de contrato.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wallets")
@CompoundIndex(name = "uk_doc_type_doc_number", def = "{'documentType': 1, 'documentNumber': 1}",
        unique = true)
public class WalletDocument {

    @Id
    private String id;

    private UUID walletUuid;

    private String documentType;

    @Indexed(unique = true)
    private String documentNumber;

    @Indexed(unique = true)
    private String phoneNumber;

    private String imei;

    private String email;

    private String status;

    private UUID linkedDebitCardId;

    /** Saldo propio del monedero. Con tarjeta vinculada se persiste siempre 0.00. */
    private BigDecimal balance;

    private String currency;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;
}
