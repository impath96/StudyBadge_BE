package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotAuthorizedPasswordAuth extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "NOT_AUTHORIZED_PASSWORD_AUTH";
    }
    @Override
    public String getMessage() {
        return "비밀번호 재설정 인증이 완료되지 않았습니다.";
    }
}