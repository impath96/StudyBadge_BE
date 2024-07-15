package com.tenten.studybadge.schedule.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tenten.studybadge.common.jsondeserializer.RepeatSituationNumberDeserializer;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepeatScheduleCreateRequest {
    @NotBlank(message = "일정 이름은 필수입니다.")
    private String scheduleName;
    @NotBlank(message = "일정 내용은 필수입니다.")
    private String scheduleContent;
    @NotNull(message = "일정 날짜는 필수입니다.")
    private LocalDate scheduleDate;
    @NotNull(message = "일정 시작 시간은 필수입니다.")
    private LocalTime scheduleStartTime;
    @NotNull(message = "일정 끝 시간은 필수입니다.")
    private LocalTime scheduleEndTime;
    @Setter
    private Long placeId;

    @NotNull(message = "반복 일정의 주기는 필수입니다.")
    private RepeatCycle repeatCycle;
    @NotNull(message = "반복 일정의 반복 상황은 필수입니다.")
    @JsonDeserialize(using = RepeatSituationNumberDeserializer.class)
    private RepeatSituation repeatSituation;
    @NotNull(message = "반복 일정의 반복 끝나는 날짜는 필수입니다.")
    private LocalDate repeatEndDate;
}
