package com.tenten.studybadge.common.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyMemberAttendanceRatioList {

    private List<StudyMemberAttendanceRatio> studyMemberAttendanceRatioList;

}