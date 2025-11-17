package com.ebbinghaus.ttopullae.global.exception;

import lombok.Getter;

@Getter
public class InfrastructureException extends RuntimeException {

    private final ExceptionCode code;

    public InfrastructureException(ExceptionCode code) {
        super(code.getDetail());
        this.code = code;
    }
}
