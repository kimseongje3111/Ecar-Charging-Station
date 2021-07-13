package com.ecar.servicestation.modules.ecar.exception.books;

public class CReserveFailedException extends RuntimeException {
    public CReserveFailedException() {
    }

    public CReserveFailedException(String message) {
        super(message);
    }

    public CReserveFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
