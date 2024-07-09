package com.tenten.studybadge.schedule.domain;

import com.tenten.studybadge.common.BaseEntity;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class Schedule extends BaseEntity {
  protected String scheduleName;
  protected String scheduleContent;
  protected LocalDate scheduleDate;
  protected LocalTime scheduleStartTime;
  protected LocalTime scheduleEndTime;
}
