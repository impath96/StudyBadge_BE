package com.tenten.studybadge.schedule.domain.repository;

import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RepeatScheduleRepository extends ScheduleRepository<RepeatSchedule> {

  @Query("SELECT rs FROM RepeatSchedule rs WHERE rs.studyChannel.id = :studyChannelId AND " +
      "(:date BETWEEN rs.scheduleDate AND rs.repeatEndDate)")
  List<RepeatSchedule> findAllByStudyChannelIdAndDate(Long studyChannelId, LocalDate date);
}
