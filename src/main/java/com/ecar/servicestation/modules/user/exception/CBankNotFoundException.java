package com.ecar.servicestation.modules.user.exception;

public class CBankNotFoundException extends RuntimeException {
    public CBankNotFoundException() {
    }

    public CBankNotFoundException(String message) {
        super(message);
    }

    public CBankNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
