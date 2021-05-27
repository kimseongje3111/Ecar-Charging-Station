package com.ecar.servicestation.infra.data.exception;

public class EVInfoServiceException extends RuntimeException {
    public EVInfoServiceException() {
    }

    public EVInfoServiceException(String message) {
        super(message);
    }

    public EVInfoServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
