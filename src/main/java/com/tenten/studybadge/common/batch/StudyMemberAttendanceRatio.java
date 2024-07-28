package com.tenten.studybadge.common.batch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyMemberAttendanceRatio {

    private Long studyMemberId;
    private Double attendanceRatio;

}