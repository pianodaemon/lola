package com.immortalcrab.as400.error;

import com.immortalcrab.as400.error.EngineError;
import com.immortalcrab.as400.error.ErrorCodes;

public class StorageError extends EngineError {

    public StorageError(String message) {
        super(message);
    }

    public StorageError(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public StorageError(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageError(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }
}
