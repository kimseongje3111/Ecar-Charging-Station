package com.ecar.servicestation.modules.user.exception.users;

public class CBookmarkNotFoundException extends RuntimeException {
    public CBookmarkNotFoundException() {
    }

    public CBookmarkNotFoundException(String message) {
        super(message);
    }

    public CBookmarkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
