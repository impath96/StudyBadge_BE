package com.tenten.studybadge.common.exception.point;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundPointException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    @Override
    public String getErrorCode() {
        return "NOT_FOUND_POINT";
    }
    @Override
    public String getMessage() {
        return "존재하지 않는 포인트 내역입니다.";
    }
}