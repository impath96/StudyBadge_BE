package com.tenten.studybadge.common.exception.participation;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class RemainingApprovalWaitingParticipationException extends AbstractException {

    private static final String ERROR_CODE = "APPROVE_WAITING_PARTICIPATION";
    private static final String ERROR_MESSAGE = "모집을 마감할 수 없습니다. 참가 신청에 승인 대기중인 것이 있는지 확인해주세요.";

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
