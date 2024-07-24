package com.tenten.studybadge.common.constant;

public class NotificationConstant {
  public static final String SINGLE_SCHEDULE_CREATE = "스터디 채널 id: %d의  %s 새로운 단일 일정이 생성되었습니다.";
  public static final String REPEAT_SCHEDULE_CREATE = "스터디 채널 id: %d의  %s 새로운 반복 일정이 생성되었습니다.";
  public static final String SINGLE_SCHEDULE_URL = "/api/study-channels/%d/single-schedules/%d";
  public static final String REPEAT_SCHEDULE_URL = "/api/study-channels/%d/repeat-schedules/%d";

  public static final String SCHEDULE_UPDATE_FOR_SINGLE_TO_SINGLE = "스터디 채널 id: %d의  %s 단일 일정이 수정되었습니다.";
  public static final String SCHEDULE_UPDATE_FOR_SINGLE_TO_REPEAT = "스터디 채널 id: %d의  %s 단일 일정이 반복 일정으로 수정되었습니다.";
  public static final String SCHEDULE_UPDATE_FOR_REPEAT_TO_REPEAT = "스터디 채널 id: %d의  %s 반복 일정이 수정되었습니다.";
  public static final String SCHEDULE_UPDATE_FOR_REPEAT_TO_SINGLE = "스터디 채널 id: %d의  %s 반복 일정이 단일 일정으로 수정되었습니다.";

  public static final String SINGLE_SCHEDULE_DELETE= "스터디 채널 id: %d의  %s 단일 일정이 삭제되었습니다.";
  public static final String REPEAT_SCHEDULE_DELETE = "스터디 채널 id: %d의  %s 반복 일정이 삭제되었습니다.";

  public static final String TEN_MINUTES_BEFORE_SCHEDULE_START = "%s 일정 시작 10분 전입니다.";
}
