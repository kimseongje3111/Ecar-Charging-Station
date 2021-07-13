package com.ecar.servicestation.modules.user.exception.users;

public class CUserNotFoundException extends RuntimeException {
    public CUserNotFoundException() {
    }

    public CUserNotFoundException(String message) {
        super(message);
    }

    public CUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
