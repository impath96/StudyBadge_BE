package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundMemberException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "NOT_FOUND_MEMBER";
    }
    @Override
    public String getMessage() {
        return "존재하지 않는 회원입니다.";
    }
}