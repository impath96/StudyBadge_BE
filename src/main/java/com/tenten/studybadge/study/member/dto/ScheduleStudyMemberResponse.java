package com.tenten.studybadge.study.member.dto;

import com.tenten.studybadge.type.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class ScheduleStudyMemberResponse {
    private Long memberId;
    private Long studyMemberId;
    private String name;
    private String imageUrl;
    @Setter
    private AttendanceStatus attendanceStatus;
    @Setter
    private boolean isAttendance;
}
