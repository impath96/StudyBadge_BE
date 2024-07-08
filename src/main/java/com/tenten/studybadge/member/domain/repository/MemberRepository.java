package com.tenten.studybadge.member.domain.repository;

import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.type.member.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAndPlatform(String email, Platform platform);

    Optional<Member> findByEmail(String username);


}
