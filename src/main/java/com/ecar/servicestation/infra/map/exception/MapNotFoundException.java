package com.ecar.servicestation.infra.map.exception;

public class MapNotFoundException extends RuntimeException {
    public MapNotFoundException() {
    }

    public MapNotFoundException(String message) {
        super(message);
    }

    public MapNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
