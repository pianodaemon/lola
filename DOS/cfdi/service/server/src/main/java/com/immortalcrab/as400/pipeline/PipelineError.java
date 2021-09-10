package com.immortalcrab.as400.pipeline;

import com.immortalcrab.as400.engine.EngineError;
import com.immortalcrab.as400.engine.ErrorCodes;

public class PipelineError extends EngineError {

    public PipelineError(String message) {
        super(message);
    }

    public PipelineError(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public PipelineError(String message, Throwable cause) {
        super(message, cause);
    }

    public PipelineError(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }
}
