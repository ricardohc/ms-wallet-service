package com.banking.wallet.exception;

/** Conflicto: documento o telefono ya registrados. Mapea a 409. */
public class WalletConflictException extends RuntimeException {

    public WalletConflictException(String message) {
        super(message);
    }
}
