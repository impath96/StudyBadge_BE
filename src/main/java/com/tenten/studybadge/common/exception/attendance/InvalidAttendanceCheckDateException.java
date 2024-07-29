package com.tenten.studybadge.common.exception.attendance;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class InvalidAttendanceCheckDateException extends AbstractException {

    private static final String ERROR_CODE = "INVALID_ATTENDANCE_CHECK_DATE";
    private static final String ERROR_MESSAGE = "해당 일정 당일에만 출석 체크가 가능합니다.";

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
