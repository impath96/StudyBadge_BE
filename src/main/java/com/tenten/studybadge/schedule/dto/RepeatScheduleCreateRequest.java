package com.tenten.studybadge.schedule.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tenten.studybadge.common.jsondeserializer.RepeatSituationNumberDeserializer;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RepeatScheduleCreateRequest extends ScheduleCreateRequest {
    private RepeatCycle repeatCycle;
    @JsonDeserialize(using = RepeatSituationNumberDeserializer.class)
    private RepeatSituation repeatSituation;
    private LocalDate repeatEndDate;

    public RepeatScheduleCreateRequest(String scheduleName, String scheduleContent,
        LocalDate startDate, LocalTime startTime, LocalTime endTime,
        RepeatCycle repeatCycle, RepeatSituation repeatSituation, LocalDate repeatEndDate,
        Long placeId) {
        this.scheduleName = scheduleName;
        this.scheduleContent = scheduleContent;
        this.scheduleDate = startDate;
        this.scheduleStartTime = startTime;
        this.scheduleEndTime = endTime;
        this.placeId = placeId;
        this.repeatCycle = repeatCycle;
        this.repeatSituation = repeatSituation;
        this.repeatEndDate = repeatEndDate;
    }
}
