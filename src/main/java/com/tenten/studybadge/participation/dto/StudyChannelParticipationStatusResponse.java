package com.tenten.studybadge.participation.dto;

import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class StudyChannelParticipationStatusResponse {

    private Long studyChannelId;
    private RecruitmentStatus recruitmentStatus;
    private List<ParticipantResponse> participants;

}
