package com.tenten.studybadge.schedule.dto;

import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponse {
  private long id;
  private long studyChannelId;
  private String scheduleName;
  private String scheduleContent;
  private LocalDate scheduleDate;
  private LocalTime scheduleStartTime;
  private LocalTime scheduleEndTime;
  private boolean isRepeated;
  private RepeatCycle repeatCycle;
  private RepeatSituation repeatSituation;
  private LocalDate repeatEndDate;
  private Long placeId;
}
