package com.tenten.studybadge.common.exception.place;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundPlaceException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    @Override
    public String getErrorCode() {
        return "NOT_FOUND_PLACE";
    }
    @Override
    public String getMessage() {
        return "존재하지 않는 장소입니다.";
    }
}