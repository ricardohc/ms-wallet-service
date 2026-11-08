package com.banking.wallet.exception;

/** Solicitud invalida del cliente. Mapea a 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}