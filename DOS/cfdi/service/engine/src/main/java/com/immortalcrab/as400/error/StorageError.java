package com.immortalcrab.as400.error;

import com.immortalcrab.as400.error.EngineError;
import com.immortalcrab.as400.error.ErrorCodes;

public class StorageError extends EngineError {

    public StorageError(String message) {
        super(message, ErrorCodes.STORAGE_PROVIDEER_ISSUES);
    }

    public StorageError(String message, Throwable cause) {
        super(message, cause, ErrorCodes.STORAGE_PROVIDEER_ISSUES);
    }
}
