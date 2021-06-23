package com.ecar.servicestation.modules.ecar.exception;

public class CReservationCancelFailedException extends RuntimeException {
    public CReservationCancelFailedException() {
    }

    public CReservationCancelFailedException(String message) {
        super(message);
    }

    public CReservationCancelFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
