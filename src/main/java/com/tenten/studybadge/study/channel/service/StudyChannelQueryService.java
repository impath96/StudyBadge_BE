package com.tenten.studybadge.study.channel.service;

import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelDepositException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.channel.dto.SearchCondition;
import com.tenten.studybadge.study.channel.dto.StudyChannelDetailsResponse;
import com.tenten.studybadge.study.channel.dto.StudyChannelListResponse;
import com.tenten.studybadge.study.deposit.domain.entity.StudyChannelDeposit;
import com.tenten.studybadge.study.deposit.domain.repository.StudyChannelDepositRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyChannelQueryService {

    private final StudyChannelRepository studyChannelRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyChannelDepositRepository studyChannelDepositRepository;
    private final MemberRepository memberRepository;

    public StudyChannelListResponse getStudyChannels(Pageable pageable, SearchCondition searchCondition) {

        Specification<StudyChannel> spec = Specification.where(null);

        if (searchCondition.getStatus() != null) {
            spec = spec.and(StudyChannelSpecification.withRecruitmentStatus(searchCondition.getStatus().name()));
        }
        if (searchCondition.getCategory() != null) {
            spec = spec.and(StudyChannelSpecification.withCategory(searchCondition.getCategory().name()));
        }
        if (searchCondition.getType() != null) {
            spec = spec.and(StudyChannelSpecification.withMeetingType(searchCondition.getType().name()));
        }

        Page<StudyChannel> studyChannels = studyChannelRepository.findAll(spec, pageable);
        List<Long> ids = studyChannels.getContent().stream()
                .map(StudyChannel::getId)
                .toList();

        List<StudyMember> leaders = studyMemberRepository.findAllWithLeader(ids, StudyMemberRole.LEADER);

        Map<Long, StudyMember> leaderMap = leaders.stream()
                .collect(Collectors.toMap(studyMember -> studyMember.getStudyChannel().getId(), Function.identity()));

        return StudyChannelListResponse.from(studyChannels, leaderMap);
    }

    public StudyChannelDetailsResponse getStudyChannel(Long studyChannelId, @Nullable Long memberId) {
        increaseViewCnt(studyChannelId);
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);

        LocalDate now = LocalDate.now();
        StudyChannelDetailsResponse.StudyChannelDetailsResponseBuilder builder = createDefaultResponseBuilder(studyChannel);

        if (memberId == null) {
            return responseForAnonymousMember(builder, studyChannel, now);
        } else {
            Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
            return responseForAuthMember(builder, studyChannel, member, now);
        }

    }

    private void increaseViewCnt(Long studyChannelId) {
        // 문제점: 기본 JPA(SimpleJpaRepository) 쿼리 메서드의 경우 exists 시 count(*) 쿼리 사용
        if (!studyChannelRepository.existsById(studyChannelId)) {
            throw new NotFoundStudyChannelException();
        }
        studyChannelRepository.increaseViewCnt(studyChannelId);
    }

    private StudyChannelDetailsResponse responseForAuthMember(
            StudyChannelDetailsResponse.StudyChannelDetailsResponseBuilder builder,
            StudyChannel studyChannel,
            Member member,
            LocalDate date) {

        if (!studyChannel.isStudyMember(member.getId())) {
            return builder.isStudyMember(false)
                    .chattingUrl(null)
                    .isStudyEnd(studyChannel.isStudyEnd(date))
                    .build();
        }

        builder.isStudyMember(true)
                .chattingUrl(studyChannel.getChattingUrl())
                .isStudyEnd(studyChannel.isStudyEnd(date))
                .isLeader(studyChannel.isLeader(member));

        if (studyChannel.isStudyEnd(date)) {
            StudyChannelDeposit deposit = studyChannelDepositRepository.findByStudyChannelIdAndMemberId(studyChannel.getId(), member.getId())
                    .orElseThrow(NotFoundStudyChannelDepositException::new);

            return builder.memberName(member.getName())
                    .attendanceRatio(deposit.getAttendanceRatio())
                    .refundsAmount(deposit.getRefundsAmount())
                    .build();
        } else {
            return builder.build();
        }
    }

    private StudyChannelDetailsResponse responseForAnonymousMember(
            StudyChannelDetailsResponse.StudyChannelDetailsResponseBuilder builder,
            StudyChannel studyChannel,
            LocalDate date) {
        return builder.isStudyMember(false)
                .chattingUrl(null)
                .isStudyEnd(studyChannel.isStudyEnd(date))
                .build();
    }

    private StudyChannelDetailsResponse.StudyChannelDetailsResponseBuilder createDefaultResponseBuilder(StudyChannel studyChannel) {
        StudyMember leader = studyChannel.getLeader();
        StudyMember subLeader = studyChannel.getSubLeader();
        return StudyChannelDetailsResponse.builder()
                .studyChannelId(studyChannel.getId())
                .studyChannelName(studyChannel.getName())
                .studyChannelDescription(studyChannel.getDescription())
                .deposit(studyChannel.getDeposit())
                .category(studyChannel.getCategory())
                .meetingType(studyChannel.getMeetingType())
                .region(studyChannel.getRegion())
                .startDate(studyChannel.getStudyDuration().getStudyStartDate())
                .endDate(studyChannel.getStudyDuration().getStudyEndDate())
                .capacity(studyChannel.getRecruitment().getRecruitmentNumber())
                .recruitmentStatus(studyChannel.getRecruitment().getRecruitmentStatus())
                .viewCnt(studyChannel.getViewCnt())
                .leaderName(leader.getMember().getName())
                .subLeaderName(Objects.requireNonNullElse(subLeader, leader).getMember().getName());
    }

}
