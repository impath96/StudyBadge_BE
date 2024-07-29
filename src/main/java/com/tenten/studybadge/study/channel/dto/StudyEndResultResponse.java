package com.tenten.studybadge.study.channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StudyEndResultResponse {

    private final Double attendanceRatio;
    private final Integer refundsAmount;
    private final String memberName;
}
