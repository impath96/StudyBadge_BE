package com.tenten.studybadge.member.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.type.member.BadgeLevel;
import com.tenten.studybadge.type.member.MemberStatus;
import com.tenten.studybadge.type.member.Platform;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    private String nickname;

    private String introduction;

    private String password;

    private String account;

    private String imgUrl;

    private String authCode;

    private Boolean auth;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    private BadgeLevel badgeLevel;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    private int point;

    private int banCnt;
}
