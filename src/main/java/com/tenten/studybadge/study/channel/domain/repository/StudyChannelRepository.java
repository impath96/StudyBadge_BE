package com.tenten.studybadge.study.channel.domain.repository;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyChannelRepository extends JpaRepository<StudyChannel, Long> {
}
