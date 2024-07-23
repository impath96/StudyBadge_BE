package com.tenten.studybadge.common.exception.studychannel;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class LeaveStudyLeaderException extends AbstractException {

    private static final String ERROR_CODE = "LEAVE_STUDY_LEADER_EXCEPTION";
    private static final String ERROR_MESSAGE = "리더는 스터디 채널에서 나갈 수 없습니다.";

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
