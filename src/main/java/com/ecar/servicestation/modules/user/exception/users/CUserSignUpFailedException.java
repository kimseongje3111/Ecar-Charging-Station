package com.ecar.servicestation.modules.user.exception.users;

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
