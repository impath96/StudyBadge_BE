package com.tenten.studybadge.common.exception.schedule;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidScheduleModificationException extends AbstractException {

  private static final String ERROR_CODE = "INVALID_SCHEDULE_MODIFICATION";
  private final String errorMessage;

  public InvalidScheduleModificationException(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return ERROR_CODE;
  }

  @Override
  public String getMessage() {
    return errorMessage;
  }
}
