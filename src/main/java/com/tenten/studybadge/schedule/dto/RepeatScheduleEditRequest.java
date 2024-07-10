package com.tenten.studybadge.schedule.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tenten.studybadge.common.jsondeserializer.RepeatSituationNumberDeserializer;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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
}
