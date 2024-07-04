package com.tenten.studybadge.study.channel.domain.entity;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudyDuration {

    private LocalDate studyStartDate;
    private LocalDate studyEndDate;

}
