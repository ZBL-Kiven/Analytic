package com.zj.analyticSdk.core.exceptions;

public class ResponseErrorException extends Exception {

    private final int httpCode;

    public ResponseErrorException(String error, int httpCode) {
        super(error);
        this.httpCode = httpCode;
    }

    public ResponseErrorException(Throwable throwable, int httpCode) {
        super(throwable);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return this.httpCode;
    }
}
