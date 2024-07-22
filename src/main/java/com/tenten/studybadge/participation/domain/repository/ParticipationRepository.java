package com.tenten.studybadge.participation.domain.repository;

import com.tenten.studybadge.participation.domain.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    boolean existsByMemberIdAndStudyChannelId(Long memberId, Long studyChannelId);

    @Query("SELECT p FROM Participation p " +
            "JOIN p.member " +
            "JOIN p.studyChannel " +
            "WHERE p.studyChannel.id = :studyChannelId")
    List<Participation> findByStudyChannelIdWithMember(Long studyChannelId);

    List<Participation> findByStudyChannelId(Long studyChannelId);

    List<Participation> findByMemberId(Long memberId);
}
