package com.tenten.studybadge.common.exception.payment;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyApprovedPaymentException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "ALREADY_APPROVED";
    }
    @Override
    public String getMessage() {
        return "이미 승인된 결제입니다.";
    }
}