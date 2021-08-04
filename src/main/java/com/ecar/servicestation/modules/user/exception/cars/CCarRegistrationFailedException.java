package com.ecar.servicestation.modules.user.exception.cars;

public class CCarRegistrationFailedException extends RuntimeException {
    public CCarRegistrationFailedException() {
    }

    public CCarRegistrationFailedException(String message) {
        super(message);
    }

    public CCarRegistrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
