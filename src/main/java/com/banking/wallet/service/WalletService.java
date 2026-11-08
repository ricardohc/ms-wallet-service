package com.banking.wallet.service;

import com.banking.wallet.dto.WalletResponse;
import com.banking.wallet.dto.WalletTransferRequest;
import com.banking.wallet.dto.WalletUpdateRequest;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Contrato de servicio del monedero Yanki. */
public interface WalletService {

    Mono<WalletResponse> create(
            com.banking.wallet.dto.WalletCreateRequest request, UUID transactionId);

    Mono<WalletResponse> findById(UUID walletId);

    Mono<WalletResponse> update(UUID walletId, WalletUpdateRequest request, UUID transactionId);

    Mono<Void> deactivate(UUID walletId, UUID transactionId);

    Mono<java.util.UUID> transfer(
            WalletTransferRequest request, String authenticatedPhoneNumber, UUID transactionId);

    Mono<com.banking.wallet.dto.BalanceResponse> findBalance(String authenticatedPhoneNumber);
}
