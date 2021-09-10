package com.immortalcrab.as400.engine;

public class EngineError extends Exception {

    protected int errorCode;

    public EngineError(String message) {
        super(message);
    }

    public EngineError(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public EngineError(String message, Throwable cause) {
        super(message, cause);
    }

    public EngineError(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }

    public int getErrorCode() {
        return errorCode;
    }
}
