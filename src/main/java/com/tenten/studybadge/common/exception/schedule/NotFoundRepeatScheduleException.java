package com.tenten.studybadge.common.exception.schedule;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundRepeatScheduleException extends AbstractException {

    private static final String ERROR_CODE = "NOT_FOUND_REPEAT_SCHEDULE";
    private static final String ERROR_MESSAGE = "존재하지 않는 반복 일정입니다.";

    @Override
    public HttpStatus getHttpStatus() {
        return NOT_FOUND;
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