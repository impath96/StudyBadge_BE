package com.tenten.studybadge.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SingleScheduleCreateRequest extends ScheduleCreateRequest {
  public SingleScheduleCreateRequest(String scheduleName, String scheduleContent,
      LocalDate startDate, LocalTime startTime, LocalTime endTime,
      Long placeId) {
    this.scheduleName = scheduleName;
    this.scheduleContent = scheduleContent;
    this.scheduleDate = startDate;
    this.scheduleStartTime = startTime;
    this.scheduleEndTime = endTime;
    this.placeId = placeId;
  }
}
