package com.tenten.studybadge.common.exception.notification;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotificationContentEmptyException extends AbstractException {

    private static final String ERROR_CODE = "NOTIFICATION_CONTENT_EMPTY";
    private static final String ERROR_MESSAGE = "알림 내용이 비어있습니다.";

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