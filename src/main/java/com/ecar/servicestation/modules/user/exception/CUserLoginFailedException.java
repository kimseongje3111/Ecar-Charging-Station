package com.ecar.servicestation.modules.user.exception;

public class CUserLoginFailedException extends RuntimeException {
    public CUserLoginFailedException() {
    }

    public CUserLoginFailedException(String message) {
        super(message);
    }

    public CUserLoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
