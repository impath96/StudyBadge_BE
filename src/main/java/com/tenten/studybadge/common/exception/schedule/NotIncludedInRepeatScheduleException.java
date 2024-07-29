package com.tenten.studybadge.common.exception.schedule;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotIncludedInRepeatScheduleException extends AbstractException {
    private static final String ERROR_CODE = "NOT_INCLUDED_IN_REPEAT_SCHEDULE";
    private static final String ERROR_MESSAGE = "해당 날짜는 반복 일정에 포함되지 않습니다.";

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
        return ERROR_MESSAGE;
    }
}