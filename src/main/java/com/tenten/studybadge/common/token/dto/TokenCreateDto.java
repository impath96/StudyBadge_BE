package com.tenten.studybadge.common.token.dto;

import com.tenten.studybadge.type.member.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenCreateDto {

    private String id;

    private String email;

    private MemberRole role;
}
