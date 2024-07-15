package com.tenten.studybadge.study.member.service;

import com.tenten.studybadge.common.exception.studychannel.NotStudyMemberException;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.study.member.dto.StudyMemberInfoResponse;
import com.tenten.studybadge.study.member.dto.StudyMembersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;

    public StudyMembersResponse getStudyMembers(Long studyChannelId, Long memberId) {

        if (!studyMemberRepository.existsByStudyChannelIdAndMemberId(studyChannelId, memberId)) {
            throw new NotStudyMemberException();
        }
        List<StudyMember> studyMembers = studyMemberRepository.findAllByStudyChannelIdWithMember(studyChannelId);

        boolean isLeader = studyMembers.stream()
                .anyMatch(studyMember -> studyMember.getMember().getId().equals(memberId) && studyMember.isLeader());

        List<StudyMemberInfoResponse> responses = studyMembers.stream()
                .map(StudyMember::toResponse)
                .toList();

        return StudyMembersResponse.builder()
                .studyMembers(responses)
                .isLeader(isLeader)
                .build();
    }

}
