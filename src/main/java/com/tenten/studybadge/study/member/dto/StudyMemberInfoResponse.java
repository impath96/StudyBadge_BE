package com.tenten.studybadge.study.member.dto;

import com.tenten.studybadge.type.member.BadgeLevel;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudyMemberInfoResponse {

    private Long studyMemberId;
    private Long memberId;
    private String name;
    private String imageUrl;
    private BadgeLevel badgeLevel;
    private StudyMemberRole role;

}
