package com.ecar.servicestation.modules.user.exception.banks;

public class CBankAuthFailedException extends RuntimeException {
    public CBankAuthFailedException() {
    }

    public CBankAuthFailedException(String message) {
        super(message);
    }

    public CBankAuthFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
