package com.tenten.studybadge.common.exception.schedule;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class IllegalArgumentForRepeatScheduleEditRequestException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "ILLEGAL_ARGUMENT_FOR_REPEAT_SCHEDULE_EDIT_REQUEST";
    }
    @Override
    public String getMessage() {
        return "올바르지 않은 반복 일정 수정 요청 값입니다.";
    }
}