package com.immortalcrab.as400.error;

public enum ErrorCodes {

    SUCCESS(0),
    STORAGE_PROVIDEER_ISSUES(191), // Lack interacting with storage provideer entity
    RESOURCE_NOT_FOUND(192),       // An element searched into resources is not there (images, xslt, pem keys)
    REQUEST_INVALID(193),          // It is not possible to consume request as it is comformed
    REQUEST_INCOMPLETE(198),       // Denotes a missing value in the request body expected
    DOCBUILD_ERROR(199),           // Problems related to docbuilder factory stuff (missing builders)
    PAC_PARTY_ISSUES(200);         // Lack interacting with PAC party entity

    protected int code;

    ErrorCodes(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
