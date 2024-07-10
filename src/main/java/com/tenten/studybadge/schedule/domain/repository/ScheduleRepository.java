package com.tenten.studybadge.schedule.domain.repository;

import com.tenten.studybadge.schedule.domain.Schedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScheduleRepository<T extends Schedule> extends JpaRepository<T, Long> {

  @Query("SELECT s FROM #{#entityName} s WHERE s.studyChannel.id = :studyChannelId")
  List<T> findAllByStudyChannelId(Long studyChannelId);

}