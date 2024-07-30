package com.tenten.studybadge.study.channel.domain.repository;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface StudyChannelRepository extends JpaRepository<StudyChannel, Long>, JpaSpecificationExecutor<StudyChannel> {

    @Query("select sc from StudyChannel sc " +
            "join fetch sc.studyMembers sm " +
            "join fetch sm.member m " +
            "where sc.id = :studyChannelId")
    Optional<StudyChannel> findByIdWithMember(Long studyChannelId);

    @Transactional
    @Modifying
    @Query("UPDATE StudyChannel sc SET sc.viewCnt = sc.viewCnt + 1 WHERE sc.id = :studyChannelId")
    void increaseViewCnt(Long studyChannelId);
}
