package com.ecar.servicestation.modules.ecar.exception;

public class CReservationEndFailedException extends RuntimeException {
    public CReservationEndFailedException() {
    }

    public CReservationEndFailedException(String message) {
        super(message);
    }

    public CReservationEndFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
