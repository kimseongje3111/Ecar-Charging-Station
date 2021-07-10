package com.ecar.servicestation.infra.address.exception;

public class AddressExceededException extends RuntimeException {
    public AddressExceededException() {
    }

    public AddressExceededException(String message) {
        super(message);
    }

    public AddressExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
