package com.tenten.studybadge.common.exception.payment;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidAmountException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "INVALID_AMOUNT";
    }
    @Override
    public String getMessage() {
        return "최소 충전 금액은 10,000원 입니다.";
    }
}