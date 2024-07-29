package com.tenten.studybadge.common.exception.studychannel;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class AlreadyExistsSubLeaderException extends AbstractException {

    private static final String ERROR_CODE = "ALREADY_EXISTS_SUB_LEADER";
    private static final String ERROR_MESSAGE = "이미 서브 리더가 존재합니다. 서브 리더는 스터디 채널 당 1명만 가능합니다.";

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
