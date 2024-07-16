package com.tenten.studybadge.study.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class StudyMembersResponse {

    List<StudyMemberInfoResponse> studyMembers;
    boolean isLeader;

}
