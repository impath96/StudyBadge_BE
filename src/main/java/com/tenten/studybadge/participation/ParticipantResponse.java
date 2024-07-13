package com.tenten.studybadge.participation;

import com.tenten.studybadge.type.member.BadgeLevel;
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
}
