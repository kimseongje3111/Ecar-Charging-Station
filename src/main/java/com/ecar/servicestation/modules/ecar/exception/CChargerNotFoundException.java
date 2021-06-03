package com.ecar.servicestation.modules.ecar.exception;

public class CChargerNotFoundException extends RuntimeException {
    public CChargerNotFoundException() {
    }

    public CChargerNotFoundException(String message) {
        super(message);
    }

    public CChargerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
