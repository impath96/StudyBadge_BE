package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "ALREADY_EXIST_EMAIL";
    }
    @Override
    public String getMessage() {
        return "중복된 이메일입니다.";
    }
}