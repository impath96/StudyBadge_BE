package com.tenten.studybadge.study.channel.dto;

import com.tenten.studybadge.study.channel.domain.entity.Recruitment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.entity.StudyDuration;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

@Builder
public record StudyChannelCreateRequest(
        @NotBlank(message = "스터디 채널명을 입력해주세요.")
        String name,

        @NotBlank(message = "스터디 채널 소개를 작성해주세요.")
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Integer recruitmentNumber,

        @Min(value = 3, message = "최소 3명 이상의 스터디 멤버가 필요합니다.")
        Integer minRecruitmentNumber,
        String category,
        String region,
        String meetingType,
        String chattingUrl,

        @Range(min = 10_000, max = 50_000, message = "예치금은 1만원 이상 ~ 5만원 이하로 설정 가능합니다.")
        Integer deposit,
        String depositDescription
        ) {
    public StudyChannel toEntity() {
        return StudyChannel.builder()
                .name(name())
                .description(description())
                .recruitment(Recruitment.builder()
                        .recruitmentNumber(recruitmentNumber())
                        .recruitmentStatus(RecruitmentStatus.RECRUITING)
                        .build())
                .category(Category.valueOf(category()))
                .meetingType(MeetingType.valueOf(meetingType()))
                .studyDuration(StudyDuration.builder()
                        .studyStartDate(startDate())
                        .studyEndDate(endDate())
                        .build())
                .deposit(deposit())
                .chattingUrl(chattingUrl())
                .region(region())
                .viewCnt(0)
                .build();
    }
}
