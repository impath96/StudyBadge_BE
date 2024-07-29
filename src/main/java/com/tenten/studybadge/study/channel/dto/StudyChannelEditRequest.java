package com.tenten.studybadge.study.channel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudyChannelEditRequest {

    @NotBlank(message = "스터디 채널명은 필수입니다.")
    private String name;
    @NotBlank(message = "스터디 채널 소개글은 필수입니다.")
    private String description;
    private String chattingUrl;

}
