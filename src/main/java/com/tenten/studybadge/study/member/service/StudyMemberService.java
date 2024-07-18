package com.tenten.studybadge.study.member.service;

import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.studychannel.*;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.study.member.dto.StudyMemberInfoResponse;
import com.tenten.studybadge.study.member.dto.StudyMembersResponse;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;
    private final StudyChannelRepository studyChannelRepository;
    private final MemberRepository memberRepository;

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

    public void assignStudyLeaderRole(Long studyChannelId, Long memberId, Long studyMemberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);

        checkLeader(studyChannel, member);

        StudyMember subLeader = studyChannel.getSubLeader();
        StudyMember target = getStudyMember(studyChannel, studyMemberId);

        if (subLeader != null) {
            throw new AlreadyExistsSubLeaderException();
        }

        target.setStudyMemberRole(StudyMemberRole.SUB_LEADER);
        studyMemberRepository.save(target);

    }

    private StudyMember getStudyMember(StudyChannel studyChannel, Long studyMemberId) {
        return studyChannel.getStudyMembers().stream()
                .filter(studyMember -> studyMember.getId().equals(studyMemberId))
                .findFirst()
                .orElseThrow(NotStudyMemberException::new);
    }

    private void checkLeader(StudyChannel studyChannel, Member member) {
        if (!studyChannel.isLeader(member)) {
            throw new NotStudyLeaderException();
        }
    }

}
