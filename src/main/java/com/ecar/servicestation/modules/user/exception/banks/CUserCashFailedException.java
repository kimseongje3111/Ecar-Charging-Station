package com.ecar.servicestation.modules.user.exception.banks;

public class CUserCashFailedException extends RuntimeException {
    public CUserCashFailedException() {
    }

    public CUserCashFailedException(String message) {
        super(message);
    }

    public CUserCashFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
