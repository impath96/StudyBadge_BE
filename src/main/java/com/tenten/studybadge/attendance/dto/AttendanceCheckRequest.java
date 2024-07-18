package com.tenten.studybadge.attendance.dto;

import com.tenten.studybadge.type.schedule.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class AttendanceCheckRequest {

    private ScheduleType scheduleType;
    private Long scheduleId;
    private LocalDate attendanceCheckDate;
    private List<AttendanceMember> attendanceMembers;

}
