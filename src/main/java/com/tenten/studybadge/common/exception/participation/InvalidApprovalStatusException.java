package com.tenten.studybadge.common.exception.participation;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class InvalidApprovalStatusException extends AbstractException {

    private static final String ERROR_CODE = "INVALID_APPROVAL_STATUS";
    private static final String ERROR_MESSAGE = "승인 대기중이 아닐 경우 승인을 할 수 없습니다.";

    @Override
    public HttpStatus getHttpStatus() {
        return NOT_FOUND;
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