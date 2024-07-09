package com.tenten.studybadge.common.exception.participation;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class OtherMemberParticipationCancelException extends AbstractException {

    private static final String ERROR_CODE = "OTHER_MEMBER_PARTICIPATION_CANCEL";
    private static final String ERROR_MESSAGE = "다른 회원의 참가 신청을 취소할 수 없습니다.";

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
