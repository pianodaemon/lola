package com.immortalcrab.as400.parser;

public class PairExtractorError extends Exception {

    public PairExtractorError(String message) {
        super(message);
    }

    public PairExtractorError(String message, Throwable cause) {
        super(message, cause);
    }
}
