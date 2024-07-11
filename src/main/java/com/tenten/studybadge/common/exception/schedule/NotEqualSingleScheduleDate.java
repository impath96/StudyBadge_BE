package com.tenten.studybadge.common.exception.schedule;

import com.tenten.studybadge.common.exception.basic.AbstractException;
import org.springframework.http.HttpStatus;

public class NotEqualSingleScheduleDate extends AbstractException {

  private static final String ERROR_CODE = "NOT_EQUAL_SINGLE_SCHEDULE_DATE";
  private static final String ERROR_MESSAGE = "단일 일정의 일정 날짜와 같지 않습니다.";

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
    return ERROR_MESSAGE ;
  }
}