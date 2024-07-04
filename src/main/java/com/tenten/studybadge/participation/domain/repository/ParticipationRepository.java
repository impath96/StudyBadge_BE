package com.tenten.studybadge.participation.domain.repository;

import com.tenten.studybadge.participation.domain.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    boolean existsByMemberIdAndStudyChannelId(Long memberId, Long studyChannelId);

}
