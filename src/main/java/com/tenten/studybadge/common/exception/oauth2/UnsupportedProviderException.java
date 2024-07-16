package com.tenten.studybadge.common.exception.oauth2;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class UnsupportedProviderException extends AbstractException {

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    @Override
    public String getErrorCode() {
        return "UNSUPPORTED_PROVIDER";
    }
    @Override
    public String getMessage() {
        return "지원하지 않는 플랫폼입니다.";
    }
}