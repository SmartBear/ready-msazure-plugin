package com.smartbear.msazuresupport.utils;

import java.io.IOException;

public class InvalidAuthorizationException extends IOException {

    public InvalidAuthorizationException() {
        super();
    }

    public InvalidAuthorizationException(String message) {
        super(message);
    }

    public InvalidAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAuthorizationException(Throwable cause) {
        super(cause);
    }
}
