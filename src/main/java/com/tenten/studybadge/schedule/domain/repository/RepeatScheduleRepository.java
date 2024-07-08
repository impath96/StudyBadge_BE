package com.tenten.studybadge.schedule.domain.repository;

import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepeatScheduleRepository extends JpaRepository<RepeatSchedule, Long>,
    ScheduleRepository<RepeatSchedule> {

}
