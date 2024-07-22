package com.tenten.studybadge.member.dto;

import com.tenten.studybadge.participation.domain.entity.Participation;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.type.participation.ParticipationStatus;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberApplyList {

    private Long studyChannelId;

    private String studyChannelName;

    private ParticipationStatus participationStatus;


    public static List<MemberApplyList> listToResponse(List<Participation> participation) {

        return participation.stream().map(part -> toResponse(part.getStudyChannel(), part)).collect(Collectors.toList());
    }

    public static MemberApplyList toResponse(StudyChannel studyChannel, Participation participation) {

        return MemberApplyList.builder()
                .studyChannelId(studyChannel.getId())
                .studyChannelName(studyChannel.getName())
                .participationStatus(participation.getParticipationStatus())
                .build();
    }
}