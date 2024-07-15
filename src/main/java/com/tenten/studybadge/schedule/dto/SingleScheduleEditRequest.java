package com.tenten.studybadge.schedule.dto;

import com.tenten.studybadge.type.schedule.ScheduleOriginType;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class SingleScheduleEditRequest extends ScheduleEditRequest{

    public SingleScheduleEditRequest(Long scheduleId, ScheduleOriginType originType,
        String scheduleName, String scheduleContent,
        LocalDate selectedDate, LocalTime startTime, LocalTime endTime,
        Long placeId) {
        this.scheduleId = scheduleId;
        this.originType = originType;
        this.scheduleName = scheduleName;
        this.scheduleContent = scheduleContent;
        this.selectedDate = selectedDate;
        this.scheduleStartTime = startTime;
        this.scheduleEndTime = endTime;
        this.placeId = placeId;
    }
}
