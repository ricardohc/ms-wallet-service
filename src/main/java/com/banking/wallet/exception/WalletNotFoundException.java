package com.banking.wallet.exception;

/** Monedero no encontrado. Mapea a 404. */
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String message) {
        super(message);
    }
}
