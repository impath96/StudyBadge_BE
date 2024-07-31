package com.tenten.studybadge.common.exception.member;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundMyStudyException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    @Override
    public String getErrorCode() {
        return "NOT_FOUND_STUDY";
    }
    @Override
    public String getMessage() {
        return "참여 중인 스터디가 없습니다.";
    }
}