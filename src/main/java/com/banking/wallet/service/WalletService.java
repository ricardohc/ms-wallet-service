package com.banking.wallet.service;

import com.banking.wallet.dto.WalletCreateRequest;
import com.banking.wallet.dto.WalletResponse;
import com.banking.wallet.dto.WalletUpdateRequest;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface WalletService {

    Mono<WalletResponse> create(WalletCreateRequest request, UUID transactionId, String correlationId);

    Mono<WalletResponse> findById(UUID walletId);

    Mono<WalletResponse> update(UUID walletId, WalletUpdateRequest request,
                                UUID transactionId, String correlationId);

    Mono<Void> deactivate(UUID walletId, UUID transactionId, String correlationId);
}