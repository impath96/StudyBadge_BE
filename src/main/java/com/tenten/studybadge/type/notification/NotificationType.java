package com.tenten.studybadge.type.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum NotificationType {
  SCHEDULE_CREATE("일정 생성"),
  SCHEDULE_UPDATE("일정 변경"),
  SCHEDULE_DELETE("일정 삭제"),
  TEN_MINUTES_BEFORE_ATTENDANCE("출석 체크 10분 전");

  private String description;
}
