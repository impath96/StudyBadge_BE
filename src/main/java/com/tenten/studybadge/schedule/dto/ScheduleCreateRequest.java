package com.tenten.studybadge.schedule.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SingleScheduleCreateRequest.class, name = "single"),
    @JsonSubTypes.Type(value = RepeatScheduleCreateRequest.class, name = "repeat")
})
public abstract class ScheduleCreateRequest {
  @NotBlank(message = "일정 이름은 필수입니다.")
  protected String scheduleName;
  @NotBlank(message = "일정 내용은 필수입니다.")
  protected String scheduleContent;
  @NotNull(message = "일정 날짜는 필수입니다.")
  protected LocalDate scheduleDate;
  @NotNull(message = "일정 시작 시간은 필수입니다.")
  protected LocalTime scheduleStartTime;
  @NotNull(message = "일정 끝 시간은 필수입니다.")
  protected LocalTime scheduleEndTime;
  @Setter
  protected Long placeId;
}
