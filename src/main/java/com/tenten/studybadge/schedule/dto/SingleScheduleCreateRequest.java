package com.tenten.studybadge.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class SingleScheduleCreateRequest {
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
}
