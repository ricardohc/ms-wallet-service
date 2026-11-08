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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Implementacion del contrato OpenAPI (WalletApi). Reactivo con Reactor,
 * sin block(). El origen de la transferencia y el balance se derivan del JWT
 * autenticado, nunca del body ni del query.
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
        return Mono.deferContextual(ctx -> authenticatedPhoneNumber()
                .flatMap(phone -> walletTransferRequest
                        .flatMap(req -> walletService.transfer(req, phone, xTransactionID)))
                .map(operationId -> {
                    OperationAcceptedResponse r = new OperationAcceptedResponse();
                    r.setOperationId(operationId);
                    r.setStatus("ACCEPTED");
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(r);
                }));
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> findWalletBalance(
            String xCorrelationID, String xChannelID, ServerWebExchange exchange) {
        return authenticatedPhoneNumber()
                .flatMap(phone -> walletService.findBalance(phone))
                .map(ResponseEntity::ok);
    }

    private Mono<String> authenticatedPhoneNumber() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast)
                .map(jwt -> jwt.getClaimAsString("phone_number"))
                .switchIfEmpty(Mono.error(new com.banking.wallet.exception.WalletForbiddenException(
                        "Authenticated identity missing")));
    }
}
