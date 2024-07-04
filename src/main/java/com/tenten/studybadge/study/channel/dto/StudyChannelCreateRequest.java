package com.tenten.studybadge.study.channel.dto;

import com.tenten.studybadge.study.channel.domain.entity.Recruitment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.entity.StudyDuration;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyChannelCreateRequest {

    @NotBlank(message = "스터디 채널명을 입력해주세요.")
    private String name;

    @NotBlank(message = "스터디 채널 소개를 작성해주세요.")
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer recruitmentNumber;

    @Min(value = 3, message = "최소 3명 이상의 스터디 멤버가 필요합니다.")
    private Integer minRecruitmentNumber;
    private Category category;
    private String region;
    private MeetingType meetingType;
    private String chattingUrl;

    @Range(min = 10_000, max = 50_000, message = "예치금은 1만원 이상 ~ 5만원 이하로 설정 가능합니다.")
    private Integer deposit;
    private String depositDescription;

    public StudyChannel toEntity() {
        return StudyChannel.builder()
                .name(this.name)
                .description(this.description)
                .recruitment(Recruitment.builder()
                        .recruitmentNumber(this.recruitmentNumber)
                        .recruitmentStatus(RecruitmentStatus.RECRUITING)
                        .build())
                .category(this.category)
                .meetingType(this.meetingType)
                .studyDuration(StudyDuration.builder()
                        .studyStartDate(this.startDate)
                        .studyEndDate(this.endDate)
                        .build())
                .deposit(this.deposit)
                .chattingUrl(this.chattingUrl)
                .region(this.region)
                .viewCnt(0)
                .build();
    }
}
