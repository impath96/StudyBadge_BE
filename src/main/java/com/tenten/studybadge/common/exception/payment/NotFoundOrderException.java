package com.tenten.studybadge.common.exception.payment;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundOrderException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    @Override
    public String getErrorCode() {
        return "NOT_FOUND_ORDER";
    }
    @Override
    public String getMessage() {
        return "주문번호에 해당하는 주문이 없습니다.";
    }
}