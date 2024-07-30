package com.tenten.studybadge.common.exception.studychannel;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class NotFoundStudyChannelDepositException extends AbstractException {

    private static final String ERROR_CODE = "NOT_FOUND_STUDY_CHANNEL";
    private static final String ERROR_MESSAGE = "존재하지 않는 예치금입니다.";

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
