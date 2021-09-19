package com.immortalcrab.as400.error;

import com.immortalcrab.as400.error.EngineError;
import com.immortalcrab.as400.error.ErrorCodes;

public class CfdiRequestError extends EngineError {

    public CfdiRequestError(String message) {
        super(message);
    }

    public CfdiRequestError(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public CfdiRequestError(String message, Throwable cause) {
        super(message, cause);
    }
}
