package com.tenten.studybadge.study.deposit.domain.repository;

import com.tenten.studybadge.study.deposit.domain.entity.StudyChannelDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyChannelDepositRepository extends JpaRepository<StudyChannelDeposit, Long> {
    Optional<StudyChannelDeposit> findByStudyChannelIdAndMemberId(Long studyChannelId, Long memberId);
    @Query("SELECT d FROM StudyChannelDeposit d " +
            "JOIN FETCH d.studyMember sm " +
            "JOIN FETCH d.member m " +
            "WHERE sm.id IN (:studyMemberIds)")
    List<StudyChannelDeposit> findAllByStudyMemberIdIn(List<Long> studyMemberIds);
}
