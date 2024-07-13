package com.tenten.studybadge.study.member.domain.repository;

import com.tenten.studybadge.study.member.domain.entity.StudyMember;
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
}
