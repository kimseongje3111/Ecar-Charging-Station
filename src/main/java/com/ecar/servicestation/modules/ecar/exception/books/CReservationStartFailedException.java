package com.ecar.servicestation.modules.ecar.exception.books;

public class CReservationStartFailedException extends RuntimeException {
    public CReservationStartFailedException() {
    }

    public CReservationStartFailedException(String message) {
        super(message);
    }

    public CReservationStartFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
