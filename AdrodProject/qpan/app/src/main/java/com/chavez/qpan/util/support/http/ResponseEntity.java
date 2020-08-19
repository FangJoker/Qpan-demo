package com.chavez.qpan.util.support.http;

public class ResponseEntity {
    String response;
    int responseCode;

    public ResponseEntity(String response, int code) {
        this.response = response;
        this.responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponse() {
        return response;
    }

}
