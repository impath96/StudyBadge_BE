package com.tenten.studybadge.study.channel.domain.repository;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudyChannelRepository extends JpaRepository<StudyChannel, Long>, JpaSpecificationExecutor<StudyChannel> {
}
