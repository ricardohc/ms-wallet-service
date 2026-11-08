package com.banking.wallet.exception;

import com.banking.wallet.dto.ErrorResponse;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/**
 * Manejo global de errores. Mapea excepciones de dominio a los codigos HTTP
 * del contrato OpenAPI con estructura ErrorResponse comun.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        List<String> details = ex.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "WALLET-400", "Invalid request",
                exchange, details);
    }

    /**
     * Captura errores de conversion de parametros y de lectura del body:
     *  - UUID invalido en header/path (X-Transaction-ID, walletId).
     *  - JSON malformado o con tipos invalidos (UUID invalido en body,
     *    campo con tipo incorrecto, etc).
     * Mapea a 400 del contrato.
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInput(
            ServerWebInputException ex, ServerWebExchange exchange) {
        Throwable cause = ex.getCause();
        String detail = cause != null ? cause.getMessage() : ex.getReason();
        if (detail == null) {
            detail = "Invalid request parameter or body";
        }
        // Recortar mensajes muy largos de Jackson
        if (detail.length() > 300) {
            detail = detail.substring(0, 300) + "...";
        }
        log.warn("Input error: {}", detail);
        return build(HttpStatus.BAD_REQUEST, "WALLET-400", detail,
                exchange, List.of(detail));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
            WalletNotFoundException ex, ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, "WALLET-404", ex.getMessage(), exchange, null);
    }

    @ExceptionHandler(WalletConflictException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleConflict(
            WalletConflictException ex, ServerWebExchange exchange) {
        return build(HttpStatus.CONFLICT, "WALLET-409", ex.getMessage(), exchange, null);
    }

    @ExceptionHandler(WalletForbiddenException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleForbidden(
            WalletForbiddenException ex, ServerWebExchange exchange) {
        return build(HttpStatus.FORBIDDEN, "WALLET-403", ex.getMessage(), exchange, null);
    }

    @ExceptionHandler(WalletUnprocessableException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnprocessable(
            WalletUnprocessableException ex, ServerWebExchange exchange) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "WALLET-422",
                ex.getMessage(), exchange, null);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "WALLET-500",
                "Internal error", exchange, null);
    }

    private Mono<ResponseEntity<ErrorResponse>> build(HttpStatus status, String code,
                                                      String message, ServerWebExchange exchange, List<String> details) {
        ErrorResponse body = new ErrorResponse();
        body.setCode(code);
        body.setMessage(message);
        body.setTimestamp(OffsetDateTime.now());
        body.setCorrelationId(exchange.getRequest().getHeaders()
                .getFirst("X-Correlation-ID"));
        body.setDetails(details);
        return Mono.just(ResponseEntity.status(status).body(body));
    }
}