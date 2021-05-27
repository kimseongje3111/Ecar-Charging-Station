package com.ecar.servicestation.infra.map.exception;

public class ReverseGeoCodingException extends RuntimeException {
    public ReverseGeoCodingException() {
    }

    public ReverseGeoCodingException(String message) {
        super(message);
    }

    public ReverseGeoCodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
