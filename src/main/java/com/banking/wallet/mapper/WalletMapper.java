package com.banking.wallet.mapper;

import com.banking.wallet.document.WalletDocument;
import com.banking.wallet.dto.WalletResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Mapea WalletDocument a DTOs de contrato. Nunca expone campos internos
 * adicionales al contrato OpenAPI.
 */
@Component
public class WalletMapper {

    public WalletDocument newWallet(
            String documentType,
            String documentNumber,
            String phoneNumber,
            String imei,
            String email) {
        return WalletDocument.builder()
                .walletUuid(UUID.randomUUID())
                .documentType(documentType)
                .documentNumber(documentNumber)
                .phoneNumber(phoneNumber)
                .imei(imei)
                .email(email)
                .status("ACTIVE")
                .balance(new BigDecimal("0.00"))
                .currency("PEN")
                .build();
    }

    public WalletResponse toResponse(WalletDocument doc, String balance) {
        WalletResponse r = new WalletResponse();
        r.setId(doc.getWalletUuid());
        r.setPhoneNumber(doc.getPhoneNumber());
        r.setEmail(doc.getEmail());
        r.setStatus(com.banking.wallet.dto.WalletStatus.valueOf(doc.getStatus()));
        r.setLinkedDebitCardId(doc.getLinkedDebitCardId());
        r.setBalance(balance);
        r.setCurrency(doc.getCurrency());
        return r;
    }

    public WalletResponse toResponse(WalletDocument doc) {
        return toResponse(doc, doc.getBalance().toPlainString());
    }
}
