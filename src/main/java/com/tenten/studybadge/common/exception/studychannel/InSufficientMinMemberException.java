package com.tenten.studybadge.common.exception.studychannel;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class InSufficientMinMemberException extends AbstractException {

    private static final String ERROR_CODE = "IN_SUFFICIENT_MIN_MEMBER";
    private static final String ERROR_MESSAGE = "최소 3명 이상의 스터디 멤버가 필요합니다.";

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }

    @Override
    public String getMessage() {
        return ERROR_MESSAGE;
    }


}
