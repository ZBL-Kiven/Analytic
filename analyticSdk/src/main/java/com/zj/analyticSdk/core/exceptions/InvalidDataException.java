package com.zj.analyticSdk.core.exceptions;

public class InvalidDataException extends Exception {

    public InvalidDataException(String error) {
        super(error);
    }

    public InvalidDataException(Throwable throwable) {
        super(throwable);
    }

}
