package com.tenten.studybadge.study.member.domain.repository;

import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import java.util.Optional;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    @Query("SELECT sm FROM StudyMember sm " +
            "JOIN FETCH sm.member " +
            "WHERE sm.studyChannel.id IN (:studyChannelIds) " +
            "AND sm.studyMemberRole = :studyMemberRole")
    List<StudyMember> findAllWithLeader(List<Long> studyChannelIds, StudyMemberRole studyMemberRole);

    boolean existsByStudyChannelIdAndMemberId(Long studyChannelId, Long memberId);

    @Query("SELECT sm FROM StudyMember sm " +
            "JOIN FETCH sm.member " +
            "WHERE sm.studyChannel.id = :studyChannelId")
    List<StudyMember> findAllByStudyChannelIdWithMember(Long studyChannelId);

    @Query("SELECT sm FROM StudyMember sm " +
            "JOIN FETCH sm.member " +
            "WHERE sm.studyChannel.id = :studyChannelId " +
            "AND sm.studyMemberStatus = 'PARTICIPATING'")
    List<StudyMember> findAllActiveStudyMembers(Long studyChannelId);
  
    Optional<StudyMember> findByMemberIdAndStudyChannelId(Long memberId, Long studyChannelId);

    List<StudyMember> findByMemberId(Long memberId);

    @Query("SELECT sm FROM StudyMember sm " +
            "JOIN FETCH sm.studyChannel " +
            "WHERE sm.member.id = :memberId")
    List<StudyMember> findAllByMemberIdWithStudyChannel(Long memberId);

    @Query("SELECT sm FROM StudyMember sm " +
            "JOIN FETCH sm.member " +
            "WHERE sm.id = :id")
    Optional<StudyMember> findByIdWithMember(Long id);

    @Query(value = "SELECT EXISTS ( " +
        "SELECT 1 FROM study_member sm " +
        "WHERE sm.member_id = :memberId " +
        "AND sm.study_channel_id = :studyChannelId " +
        "AND sm.study_member_status = 'PARTICIPATING')",
        nativeQuery = true)
    Integer existsByMemberIdAndStudyChannelIdAndStudyMemberStatus(Long memberId, Long studyChannelId);
}
