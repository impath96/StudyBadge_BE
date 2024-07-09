package com.tenten.studybadge.schedule.domain.repository;


import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleScheduleRepository extends JpaRepository<SingleSchedule, Long>,
    ScheduleRepository<SingleSchedule> {

}
