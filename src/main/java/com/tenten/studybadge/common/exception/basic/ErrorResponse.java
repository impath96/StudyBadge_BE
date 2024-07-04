package com.tenten.studybadge.common.exception.basic;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private String errorCode;
    private String message;
}
