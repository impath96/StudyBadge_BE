package com.tenten.studybadge.common.exception.schedule;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class OutRangeScheduleException extends AbstractException {
    private static final String ERROR_CODE = "OUT_RANGE_REPEAT_SCHEDULE_SITUATION";
    private static final String ERROR_MESSAGE = "해당 반복 일정의 속한 날짜가 아닙니다.";

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