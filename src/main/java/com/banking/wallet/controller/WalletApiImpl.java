package com.banking.wallet.controller;

import com.banking.wallet.api.WalletApi;
import com.banking.wallet.dto.WalletCreateRequest;
import com.banking.wallet.dto.WalletResponse;
import com.banking.wallet.dto.WalletUpdateRequest;
import com.banking.wallet.service.WalletService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WalletApiImpl implements WalletApi {

    private final WalletService walletService;

    @Override
    public Mono<ResponseEntity<WalletResponse>> createWallet(
            String xCorrelationID, String xChannelID, UUID xTransactionID,
            Mono<WalletCreateRequest> walletCreateRequest, ServerWebExchange exchange) {
        return walletCreateRequest
                .flatMap(req -> walletService.create(req, xTransactionID, xCorrelationID))
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @Override
    public Mono<ResponseEntity<WalletResponse>> findWalletById(
            UUID walletId, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        return walletService.findById(walletId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<WalletResponse>> updateWallet(
            UUID walletId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, Mono<WalletUpdateRequest> walletUpdateRequest,
            ServerWebExchange exchange) {
        return walletUpdateRequest
                .flatMap(req -> walletService.update(walletId, req, xTransactionID, xCorrelationID))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deactivateWallet(
            UUID walletId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, ServerWebExchange exchange) {
        return walletService.deactivate(walletId, xTransactionID, xCorrelationID)
                .thenReturn(ResponseEntity.noContent().build());
    }
}