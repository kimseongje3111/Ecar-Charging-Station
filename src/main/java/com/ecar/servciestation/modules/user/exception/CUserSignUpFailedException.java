package com.ecar.servciestation.modules.user.exception;

public class CUserSignUpFailedException extends RuntimeException {
    public CUserSignUpFailedException() {
    }

    public CUserSignUpFailedException(String message) {
        super(message);
    }

    public CUserSignUpFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
