package com.tenten.studybadge.common.exception.participation;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class AlreadyAppliedParticipationException extends AbstractException {

    private static final String ERROR_CODE = "ALREADY_APPLIED_PARTICIPATION";
    private static final String ERROR_MESSAGE = "해당 스터디 채널에 이미 참가 신청한 상태입니다.";

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
