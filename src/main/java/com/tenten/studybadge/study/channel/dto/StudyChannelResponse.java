package com.tenten.studybadge.study.channel.dto;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class StudyChannelResponse {

    private Long studyChannelId;
    private String name;
    private Category category;
    private String description;
    private RecruitmentStatus recruitmentStatus;;
    private MeetingType meetingType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int deposit;
    private int viewCnt;
    private Long memberId;
    private String memberName;

    public static StudyChannelResponse from(StudyChannel studyChannel, StudyMember studyMember) {
        return StudyChannelResponse.builder()
                .studyChannelId(studyChannel.getId())
                .name(studyChannel.getName())
                .category(studyChannel.getCategory())
                .description(studyChannel.getDescription())
                .recruitmentStatus(studyChannel.getRecruitment().getRecruitmentStatus())
                .meetingType(studyChannel.getMeetingType())
                .startDate(studyChannel.getStudyDuration().getStudyStartDate())
                .endDate(studyChannel.getStudyDuration().getStudyEndDate())
                .deposit(studyChannel.getDeposit())
                .viewCnt(studyChannel.getViewCnt())
                .memberId(studyMember.getMember().getId())
                .memberName(studyMember.getMember().getName())
                .build();
    }

}
