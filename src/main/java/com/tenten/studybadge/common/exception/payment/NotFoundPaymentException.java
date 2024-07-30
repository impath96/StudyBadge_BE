package com.tenten.studybadge.common.exception.payment;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundPaymentException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    @Override
    public String getErrorCode() {
        return "NOT_FOUND_PAYMENT";
    }
    @Override
    public String getMessage() {
        return "결제내역이 존재하지 않습니다.";
    }
}