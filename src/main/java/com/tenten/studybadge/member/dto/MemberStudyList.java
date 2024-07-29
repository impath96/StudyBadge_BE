package com.tenten.studybadge.member.dto;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import lombok.*;

import java.util.List;
import java.util.function.Function;
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

    private double attendanceRatio;

    public static List<MemberStudyList> listToResponse(List<StudyMember> studyMembers, Function<StudyMember, Double> attendanceCalculator) {
        return studyMembers.stream()
                .map(member -> toResponse(member.getStudyChannel(), member, attendanceCalculator))
                .collect(Collectors.toList());
    }

    public static MemberStudyList toResponse(StudyChannel studyChannel, StudyMember studyMember, Function<StudyMember, Double> attendanceCalculator) {

        return MemberStudyList.builder()
                .studyName(studyChannel.getName())
                .studyId(studyChannel.getId())
                .role(studyMember.getStudyMemberRole())
                .attendanceRatio(attendanceCalculator.apply(studyMember))
                .build();
    }
}