package com.tenten.studybadge.study.member.domain.repository;

import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
}
