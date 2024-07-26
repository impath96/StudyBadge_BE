package com.tenten.studybadge.member.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MemberUpdateRequest {

    private String nickname;

    private String account;

    private String accountBank;

    private String introduction;

    private String imgUrl;
}