package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidAuthCodeException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "INVALID_AUTH_CODE";
    }
    @Override
    public String getMessage() {
        return "유효하지 않는 인증 코드입니다.";
    }
}