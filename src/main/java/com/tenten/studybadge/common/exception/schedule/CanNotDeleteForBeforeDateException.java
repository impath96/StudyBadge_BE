package com.tenten.studybadge.common.exception.schedule;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class CanNotDeleteForBeforeDateException extends AbstractException {
    private static final String ERROR_CODE = "CAN_NOT_DELETE_FOR_BEFORE_DATE_EXCEPTION";
    private static final String ERROR_MESSAGE = "이전 날짜는 삭제할 수 없습니다.";

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