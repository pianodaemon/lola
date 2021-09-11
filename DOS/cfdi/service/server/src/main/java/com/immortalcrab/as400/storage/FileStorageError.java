package com.immortalcrab.as400.storage;

import com.immortalcrab.as400.engine.EngineError;
import com.immortalcrab.as400.engine.ErrorCodes;

public class FileStorageError extends EngineError {

    public FileStorageError(String message) {
        super(message);
    }

    public FileStorageError(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public FileStorageError(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStorageError(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }
}
