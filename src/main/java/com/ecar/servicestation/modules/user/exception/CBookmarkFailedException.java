package com.ecar.servicestation.modules.user.exception;

public class CBookmarkFailedException extends RuntimeException {
    public CBookmarkFailedException() {
    }

    public CBookmarkFailedException(String message) {
        super(message);
    }

    public CBookmarkFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
