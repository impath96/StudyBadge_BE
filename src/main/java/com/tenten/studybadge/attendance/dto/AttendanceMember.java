package com.tenten.studybadge.attendance.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AttendanceMember {
    private Long studyMemberId;
    private Boolean isAttendance;
}

