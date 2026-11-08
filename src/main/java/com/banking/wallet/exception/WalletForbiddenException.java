package com.banking.wallet.exception;

/** El origen de la transferencia no corresponde al JWT autenticado. Mapea a 403. */
public class WalletForbiddenException extends RuntimeException {

    public WalletForbiddenException(String message) {
        super(message);
    }
}
