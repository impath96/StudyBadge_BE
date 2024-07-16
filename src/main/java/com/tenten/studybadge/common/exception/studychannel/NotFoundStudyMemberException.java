package com.tenten.studybadge.common.exception.studychannel;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundStudyMemberException extends AbstractException {

    private static final String ERROR_CODE = "NOT_STUDY_MEMBER";
    private static final String ERROR_MESSAGE = "스터디 멤버를 찾지 못했습니다.";

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
