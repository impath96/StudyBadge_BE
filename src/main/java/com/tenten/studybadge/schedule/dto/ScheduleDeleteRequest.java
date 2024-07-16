package com.tenten.studybadge.schedule.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDeleteRequest {
  @NotNull(message = "회원 id는 필수입니다.")
  private Long memberId;
  @NotNull(message = "삭제할 일정의 id는 필수입니다.")
  private Long scheduleId;

  @NotNull(message = "삭제할 일정의 선택한 날짜는 필수입니다")
  private LocalDate selectedDate;
}
