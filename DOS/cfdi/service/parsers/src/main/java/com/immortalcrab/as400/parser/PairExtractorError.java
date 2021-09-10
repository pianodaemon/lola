package com.immortalcrab.as400.parser;

import com.immortalcrab.as400.engine.EngineError;
import com.immortalcrab.as400.engine.ErrorCodes;

public class PairExtractorError extends EngineError {

    public PairExtractorError(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public PairExtractorError(String message, Throwable cause) {
        super(message, cause);
    }

    public PairExtractorError(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }
}
