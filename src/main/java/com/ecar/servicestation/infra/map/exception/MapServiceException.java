package com.ecar.servicestation.infra.map.exception;

public class MapServiceException extends RuntimeException {
    public MapServiceException() {
    }

    public MapServiceException(String message) {
        super(message);
    }

    public MapServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
