package com.tenten.studybadge.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AttendanceInfoResponse {

    private Long memberId;
    private Long studyMemberId;
    private String name;
    private String imageUrl;
    private long attendanceCount;
    private double attendanceRatio;

}
