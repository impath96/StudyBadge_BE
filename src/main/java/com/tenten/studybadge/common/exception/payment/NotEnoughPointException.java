package com.tenten.studybadge.common.exception.payment;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotEnoughPointException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "NOT_ENOUGH_POINT";
    }
    @Override
    public String getMessage() {
        return "포인트가 충분하지 않습니다.";
    }
}