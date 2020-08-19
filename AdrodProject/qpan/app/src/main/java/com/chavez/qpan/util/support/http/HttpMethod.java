package com.chavez.qpan.util.support.http;

public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS");

    public String value;

    String getValue() {
        return value;
    }

    void setValue(String v) {
        this.value = v;
    }

    HttpMethod(String v) {
        this.value = v;
    }
}
