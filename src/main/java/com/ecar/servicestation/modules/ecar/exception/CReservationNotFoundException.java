package com.ecar.servicestation.modules.ecar.exception;

public class CReservationNotFoundException extends RuntimeException {
    public CReservationNotFoundException() {
    }

    public CReservationNotFoundException(String message) {
        super(message);
    }

    public CReservationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
