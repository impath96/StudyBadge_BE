package com.tenten.studybadge.common.exception;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "INVALID_TOKEN";
    }
    @Override
    public String getMessage() {
        return "유효하지 않은 토큰입니다.";
    }
}