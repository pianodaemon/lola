package com.immortalcrab.as400.error;

import com.immortalcrab.as400.error.EngineError;
import com.immortalcrab.as400.error.ErrorCodes;

public class FormatError extends EngineError {

    public FormatError(String message) {
        super(message);
    }

    public FormatError(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public FormatError(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatError(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }
}
