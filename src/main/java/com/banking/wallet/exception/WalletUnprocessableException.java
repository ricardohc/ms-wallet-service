package com.banking.wallet.exception;

/** Entidad no procesable (ej. tarjeta inexistente o no ACTIVE). Mapea a 422. */
public class WalletUnprocessableException extends RuntimeException {

    public WalletUnprocessableException(String message) {
        super(message);
    }
}
