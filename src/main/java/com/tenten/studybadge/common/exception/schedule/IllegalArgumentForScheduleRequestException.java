package com.tenten.studybadge.common.exception.schedule;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class IllegalArgumentForScheduleRequestException extends AbstractException {

    private static final String ERROR_CODE = "ILLEGAL_ARGUMENT_FOR_SCHEDULE_REQUEST";
    private static final String ERROR_MESSAGE = "올바르지 않은 일정 요청 값입니다.";

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
    @Override
    public String getMessage() {
        return ERROR_MESSAGE ;
    }
}