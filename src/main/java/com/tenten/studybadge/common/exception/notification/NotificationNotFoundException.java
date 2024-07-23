package com.tenten.studybadge.common.exception.notification;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends AbstractException {

    private static final String ERROR_CODE = "NOTIFICATION_NOT_FOUND";
    private static final String ERROR_MESSAGE = "알림이 존재하지 않습니다.";

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_REQUEST ;
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