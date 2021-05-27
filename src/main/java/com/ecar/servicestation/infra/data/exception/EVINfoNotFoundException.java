package com.ecar.servicestation.infra.data.exception;

public class EVINfoNotFoundException extends RuntimeException {
    public EVINfoNotFoundException() {
    }

    public EVINfoNotFoundException(String message) {
        super(message);
    }

    public EVINfoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
