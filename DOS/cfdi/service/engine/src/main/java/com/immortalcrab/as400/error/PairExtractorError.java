package com.immortalcrab.as400.error;

public class PairExtractorError extends EngineError {

    public PairExtractorError(String message) {
        super(message, ErrorCodes.REQUEST_INVALID);
    }

    public PairExtractorError(String message, Throwable cause) {
        super(message, cause, ErrorCodes.REQUEST_INVALID);
    }
}
