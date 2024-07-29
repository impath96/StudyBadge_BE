package com.tenten.studybadge.participation.domain.repository;

import com.tenten.studybadge.participation.domain.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    boolean existsByMemberIdAndStudyChannelId(Long memberId, Long studyChannelId);

    @Query("SELECT p FROM Participation p " +
            "JOIN FETCH p.member " +
            "JOIN FETCH p.studyChannel " +
            "WHERE p.studyChannel.id = :studyChannelId")
    List<Participation> findByStudyChannelIdWithMember(Long studyChannelId);

    List<Participation> findByStudyChannelId(Long studyChannelId);

    @Query("SELECT p FROM Participation p " +
            "JOIN FETCH p.studyChannel " +
            "WHERE p.member.id = :memberId" )
    List<Participation> findAllByMemberIdWithStudyChannel(Long memberId);

    @Query("SELECT p FROM Participation p " +
            "JOIN FETCH p.member " +
            "WHERE p.id = :id")
    Optional<Participation> findByIdWithMember(Long id);
}
