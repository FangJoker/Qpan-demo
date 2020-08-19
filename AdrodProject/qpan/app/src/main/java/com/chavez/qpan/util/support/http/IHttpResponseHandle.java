package com.chavez.qpan.util.support.http;

import com.chavez.qpan.exception.HttpStopRequestException;

public interface IHttpResponseHandle {
    void success(ResponseEntity entity);
    void error(ResponseEntity entity);

}
