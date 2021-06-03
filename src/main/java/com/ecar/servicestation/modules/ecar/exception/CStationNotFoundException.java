package com.ecar.servicestation.modules.ecar.exception;

public class CStationNotFoundException extends RuntimeException {
    public CStationNotFoundException() {
    }

    public CStationNotFoundException(String message) {
        super(message);
    }

    public CStationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
