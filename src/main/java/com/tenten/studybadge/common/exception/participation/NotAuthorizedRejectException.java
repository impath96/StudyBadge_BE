package com.tenten.studybadge.common.exception.participation;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class NotAuthorizedRejectException extends AbstractException {

    private static final String ERROR_CODE = "NOT_AUTHORIZED_REJECT";
    private static final String ERROR_MESSAGE = "거절은 해당 스터디의 리더만 가능합니다.";

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
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
