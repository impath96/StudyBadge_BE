package com.tenten.studybadge.common.exception.schedule;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class IllegalArgumentForRepeatSituationException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "ILLEGAL_ARGUMENT_FOR_REPEAT_SITUATION";
    }
    @Override
    public String getMessage() {
        return "올바르지 않은 월간 반복 요청 값입니다..";
    }
}