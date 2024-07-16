package com.tenten.studybadge.schedule.domain;

import com.tenten.studybadge.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class Schedule extends BaseEntity {
    @Column(length = 60)
    protected String scheduleName;
    @Column(length = 210)
    protected String scheduleContent;
    protected LocalDate scheduleDate;
    protected LocalTime scheduleStartTime;
    protected LocalTime scheduleEndTime;
}
