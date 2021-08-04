package com.ecar.servicestation.modules.user.exception.banks;

public class CBankExistsException extends RuntimeException {
    public CBankExistsException() {
    }

    public CBankExistsException(String message) {
        super(message);
    }

    public CBankExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
