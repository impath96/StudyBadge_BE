package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class ExistPointException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "EXIST_POINT";
    }
    @Override
    public String getMessage() {
        return "잔여 포인트를 출금하시고 회원 탈퇴를 진행해주세요.";
    }
}