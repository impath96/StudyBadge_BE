package com.tenten.studybadge.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
