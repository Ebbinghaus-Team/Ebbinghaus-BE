package com.ebbinghaus.ttopullae.global.exception;

import org.springframework.http.HttpStatus;

public interface ExceptionCode {
    HttpStatus getHttpStatus();
    String getTitle();
    String getDetail();
}
