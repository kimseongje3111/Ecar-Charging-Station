package com.ecar.servicestation.infra.address.exception;

public class AddressServiceException extends RuntimeException {
    public AddressServiceException() {
    }

    public AddressServiceException(String message) {
        super(message);
    }

    public AddressServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
