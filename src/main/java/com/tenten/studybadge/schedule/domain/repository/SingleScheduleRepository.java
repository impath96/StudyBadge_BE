package com.tenten.studybadge.schedule.domain.repository;


import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleScheduleRepository extends ScheduleRepository<SingleSchedule> {
  @Query("SELECT ss FROM SingleSchedule ss WHERE ss.studyChannel.id = :studyChannelId AND ss.scheduleDate BETWEEN :startDate AND :endDate")
  List<SingleSchedule> findAllByStudyChannelIdAndDateRange(Long studyChannelId, LocalDate startDate, LocalDate endDate);
}
