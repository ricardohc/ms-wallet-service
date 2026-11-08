package com.banking.wallet.controller;

import com.banking.wallet.api.WalletApi;
import com.banking.wallet.dto.BalanceResponse;
import com.banking.wallet.dto.OperationAcceptedResponse;
import com.banking.wallet.dto.WalletCreateRequest;
import com.banking.wallet.dto.WalletResponse;
import com.banking.wallet.dto.WalletTransferRequest;
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

/**
 * Implementacion del contrato OpenAPI (WalletApi). Reactivo con Reactor,
 * sin block().
 *
 * NOTA: Se elimino la validacion de identidad via JWT en los endpoints
 * createWalletTransfer y findWalletBalance. Ahora el origen de la
 * transferencia viaja en el body (sourcePhoneNumber) y el phoneNumber
 * del balance viaja como query param. Esto es solo para pruebas locales.
 */
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
                .flatMap(req -> walletService.create(req, xTransactionID))
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
                .flatMap(req -> walletService.update(walletId, req, xTransactionID))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deactivateWallet(
            UUID walletId, String xCorrelationID, String xChannelID,
            UUID xTransactionID, ServerWebExchange exchange) {
        return walletService.deactivate(walletId, xTransactionID)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<OperationAcceptedResponse>> createWalletTransfer(
            String xCorrelationID, String xChannelID, UUID xTransactionID,
            Mono<WalletTransferRequest> walletTransferRequest, ServerWebExchange exchange) {
        return walletTransferRequest
                .flatMap(req -> walletService.transfer(
                        req, req.getSourcePhoneNumber(), xTransactionID))
                .map(operationId -> {
                    OperationAcceptedResponse r = new OperationAcceptedResponse();
                    r.setOperationId(operationId);
                    r.setStatus("ACCEPTED");
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(r);
                });
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> findWalletBalance(
            String phoneNumber, String xCorrelationID, String xChannelID,
            ServerWebExchange exchange) {
        return walletService.findBalance(phoneNumber)
                .map(ResponseEntity::ok);
    }
}