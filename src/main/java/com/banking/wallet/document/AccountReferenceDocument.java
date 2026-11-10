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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "account_reference")
public class AccountReferenceDocument {

    @Id
    private String id;

    @Indexed
    private String accountNumber;

    @Indexed
    private UUID customerId;

    private String accountType;

    private String profile;

    private String status;

    private BigDecimal balance;

    private String currency;

    private Instant eventCreatedAt;

    private Instant occurredAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;
}