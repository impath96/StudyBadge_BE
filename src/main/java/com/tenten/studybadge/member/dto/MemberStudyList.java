package com.tenten.studybadge.member.dto;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStudyList {

    private Long studyId;

    private String studyName;

    private StudyMemberRole role;

    public static List<MemberStudyList> listToResponse(List<StudyMember> studyMembers) {
        return studyMembers.stream()
                .map(member -> toResponse(member.getStudyChannel(), member))
                .collect(Collectors.toList());
    }

    public static MemberStudyList toResponse(StudyChannel studyChannel, StudyMember studyMember) {

        return MemberStudyList.builder()
                .studyName(studyChannel.getName())
                .studyId(studyChannel.getId())
                .role(studyMember.getStudyMemberRole())
                .build();
    }
}