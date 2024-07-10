package com.tenten.studybadge.schedule.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tenten.studybadge.common.jsondeserializer.RepeatSituationNumberDeserializer;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.type.schedule.ScheduleOriginType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RepeatScheduleEditRequest extends ScheduleEditRequest{
  private boolean isAfterEventSame;
  @NotNull(message = "일정 반복 주기는 필수입니다.")
  private RepeatCycle repeatCycle;
  @JsonDeserialize(using = RepeatSituationNumberDeserializer.class)
  private RepeatSituation repeatSituation;
  @NotNull(message = "일정 반복 끝나는 날짜는 필수입니다.")
  private LocalDate repeatEndDate;

  public RepeatScheduleEditRequest(Long scheduleId, ScheduleOriginType originType,
      String scheduleName, String scheduleContent,
      LocalDate selectedDate, LocalTime startTime, LocalTime endTime,
      boolean isAfterEventSame,
      RepeatCycle repeatCycle, RepeatSituation repeatSituation, LocalDate repeatEndDate,
      Long placeId) {
    this.scheduleId = scheduleId;
    this.originType = originType;
    this.scheduleName = scheduleName;
    this.scheduleContent = scheduleContent;
    this.selectedDate = selectedDate;
    this.scheduleStartTime = startTime;
    this.scheduleEndTime = endTime;
    this.isAfterEventSame = isAfterEventSame;
    this.repeatCycle = repeatCycle;
    this.repeatSituation = repeatSituation;
    this.repeatEndDate = repeatEndDate;
    this.placeId = placeId;
  }
}
