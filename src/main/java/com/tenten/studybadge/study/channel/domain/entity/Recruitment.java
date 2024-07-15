package com.tenten.studybadge.study.channel.domain.entity;

import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Recruitment {

    private static final Integer MIN_RECRUITMENT_NUMBER = 3;

    private Integer recruitmentNumber;

    @Enumerated(EnumType.STRING)
    private RecruitmentStatus recruitmentStatus;

    public boolean isCompleted() {
        return Objects.equals(recruitmentStatus, RecruitmentStatus.RECRUIT_COMPLETED);
    }

    public void start() {
        this.recruitmentStatus = RecruitmentStatus.RECRUITING;
    }

}
