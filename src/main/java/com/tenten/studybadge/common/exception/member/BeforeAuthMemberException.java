package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class BeforeAuthMemberException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "WAIT_FOR_APPROVAL";
    }
    @Override
    public String getMessage() {
        return "인증 진행 중인 회원입니다.";
    }
}