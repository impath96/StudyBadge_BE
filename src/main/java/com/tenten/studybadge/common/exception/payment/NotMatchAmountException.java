package com.tenten.studybadge.common.exception.payment;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotMatchAmountException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "NOT_MATCH_AMOUNT";
    }
    @Override
    public String getMessage() {
        return "주문 금액과 요청한 금액이 일치하지 않습니다.";
    }
}