package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyAuthException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "APPROVED_MEMBER";
    }
    @Override
    public String getMessage() {
        return "이미 인증된 회원입니다.";
    }
}