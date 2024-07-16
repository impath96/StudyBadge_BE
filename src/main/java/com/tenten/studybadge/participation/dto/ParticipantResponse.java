package com.tenten.studybadge.participation.dto;

import com.tenten.studybadge.type.member.BadgeLevel;
import com.tenten.studybadge.type.participation.ParticipationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ParticipantResponse {
    private Long memberId;
    private String imageUrl;
    private String name;
    private int banCnt;
    private BadgeLevel badgeLevel;
    private Long participationId;
    private ParticipationStatus participationStatus;
}
