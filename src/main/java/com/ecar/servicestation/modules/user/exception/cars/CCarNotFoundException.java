package com.ecar.servicestation.modules.user.exception.cars;

public class CCarNotFoundException extends RuntimeException {
    public CCarNotFoundException() {
    }

    public CCarNotFoundException(String message) {
        super(message);
    }

    public CCarNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
