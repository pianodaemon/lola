package com.immortalcrab.as400.request;

public class CfdiRequestError extends Exception {

    public CfdiRequestError(String message) {
        super(message);
    }

    public CfdiRequestError(String message, Throwable cause) {
        super(message, cause);
    }
}
