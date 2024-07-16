package com.tenten.studybadge.common.exception.studychannel;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class NotChangeRecruitmentStatusException extends AbstractException {

    private static final String ERROR_CODE = "NOT_CHANGE_RECRUITMENT_STATUS";
    private static final String ERROR_MESSAGE = "모집 상태를 변경할 수 없습니다.";

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
