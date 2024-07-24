package com.tenten.studybadge.study.deposit.domain.repository;

import com.tenten.studybadge.study.deposit.domain.entity.StudyChannelDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyChannelDepositRepository extends JpaRepository<StudyChannelDeposit, Long> {
    Optional<StudyChannelDeposit> findByStudyChannelIdAndMemberId(Long studyChannelId, Long memberId);
}
