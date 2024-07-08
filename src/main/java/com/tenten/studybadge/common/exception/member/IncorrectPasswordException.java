package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class IncorrectPasswordException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "INCORRECT_PASSWORD";
    }
    @Override
    public String getMessage() {
        return "비밀번호가 일치하지 않습니다.";
    }
}