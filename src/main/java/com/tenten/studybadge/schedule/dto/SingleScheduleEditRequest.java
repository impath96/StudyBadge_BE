package com.tenten.studybadge.schedule.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tenten.studybadge.common.jsondeserializer.ScheduleTypeDeserializer;
import com.tenten.studybadge.type.schedule.ScheduleType;
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
@NoArgsConstructor
@AllArgsConstructor
public class SingleScheduleEditRequest implements ScheduleEditRequest{
    @NotNull(message = "회원 id는 필수입니다.")
    private Long memberId;
    @NotNull(message = "수정할 일정 id는 필수입니다.")
    private long scheduleId;
    @NotNull(message = "기존 일정 타입은 필수입니다.")
    @JsonDeserialize(using = ScheduleTypeDeserializer.class)
    private ScheduleType originType;
    @NotNull(message = "수정할 일정 타입은 필수입니다.")
    private ScheduleType editType;
    @NotBlank(message = "일정 이름은 필수입니다.")
    protected String scheduleName;
    @NotBlank(message = "일정 내용은 필수입니다.")
    private String scheduleContent;
    @NotNull(message = "수정할 일정 날짜는 필수입니다.")
    private LocalDate selectedDate;
    @NotNull(message = "일정 시작 시간은 필수입니다.")
    private LocalTime scheduleStartTime;
    @NotNull(message = "일정 끝 시간은 필수입니다.")
    private LocalTime scheduleEndTime;
    @Setter
    private Long placeId;

    @Override
    public ScheduleType getEditType() {
        return editType;
    }
}
