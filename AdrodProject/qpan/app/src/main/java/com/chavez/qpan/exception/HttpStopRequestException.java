package com.chavez.qpan.exception;

import androidx.annotation.Nullable;

public class HttpStopRequestException extends Exception {
    private int httpStatusCode;
    private String message;

    public HttpStopRequestException(int code, String msg) {
        this.httpStatusCode = code;
        this.message = msg;
    }

    @Nullable
    @Override
    public String getMessage() {
        return message + "on http" + httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
