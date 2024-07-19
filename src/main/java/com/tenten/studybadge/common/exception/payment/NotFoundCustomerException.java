package com.tenten.studybadge.common.exception.payment;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundCustomerException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    @Override
    public String getErrorCode() {
        return "NOT_FOUND_CUSTOMER";
    }
    @Override
    public String getMessage() {
        return "고객을 찾을 수 없습니다.";
    }
}