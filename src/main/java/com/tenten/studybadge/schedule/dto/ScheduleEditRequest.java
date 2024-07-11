package com.tenten.studybadge.schedule.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tenten.studybadge.common.jsondeserializer.RepeatSituationNumberDeserializer;
import com.tenten.studybadge.common.jsondeserializer.ScheduleOriginTypeDeserializer;
import com.tenten.studybadge.type.schedule.ScheduleOriginType;
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
    @JsonSubTypes.Type(value = SingleScheduleEditRequest.class, name = "single"),
    @JsonSubTypes.Type(value = RepeatScheduleEditRequest.class, name = "repeat")
})
public abstract class ScheduleEditRequest {
    @NotNull(message = "수정할 일정 id는 필수입니다.")
    protected long scheduleId;
    @NotNull(message = "기존 일정 타입은 필수입니다.")
    @JsonDeserialize(using = ScheduleOriginTypeDeserializer.class)
    protected ScheduleOriginType originType;
    @NotBlank(message = "일정 이름은 필수입니다.")
    protected String scheduleName;
    @NotBlank(message = "일정 내용은 필수입니다.")
    protected String scheduleContent;
    @NotNull(message = "수정할 일정 날짜는 필수입니다.")
    protected LocalDate selectedDate;
    @NotNull(message = "일정 시작 시간은 필수입니다.")
    protected LocalTime scheduleStartTime;
    @NotNull(message = "일정 끝 시간은 필수입니다.")
    protected LocalTime scheduleEndTime;
    @Setter
    protected Long placeId;
}
