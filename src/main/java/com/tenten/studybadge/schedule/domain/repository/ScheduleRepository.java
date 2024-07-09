package com.tenten.studybadge.schedule.domain.repository;

import com.tenten.studybadge.schedule.domain.Schedule;


public interface ScheduleRepository<T extends Schedule> {
  T save(T entity);
}