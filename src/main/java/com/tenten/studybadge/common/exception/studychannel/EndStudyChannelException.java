package com.tenten.studybadge.common.exception.studychannel;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class EndStudyChannelException extends AbstractException {

    private static final String ERROR_CODE = "END_STUDY_CHANNEL";
    private static final String ERROR_MESSAGE = "이미 종료된 스터디 채널입니다.";

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
