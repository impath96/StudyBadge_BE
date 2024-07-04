package com.tenten.studybadge.study.channel.domain.entity;

import com.tenten.studybadge.common.exception.studychannel.InvalidStudyDurationException;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyDuration {

    private LocalDate studyStartDate;
    private LocalDate studyEndDate;

    @Builder
    public StudyDuration(LocalDate studyStartDate, LocalDate studyEndDate) {
        if (studyStartDate.isAfter(studyEndDate)) {
            throw new InvalidStudyDurationException();
        }
        this.studyStartDate = studyStartDate;
        this.studyEndDate = studyEndDate;
    }

    public boolean isStartDateBeforeTo(LocalDate date) {
        return studyStartDate.isBefore(date);
    }

}
