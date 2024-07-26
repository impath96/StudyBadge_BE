package com.tenten.studybadge.study.channel.service;

import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.studychannel.InvalidStudyStartDateException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.common.exception.studychannel.NotStudyLeaderException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.notification.service.NotificationSchedulerService;
import com.tenten.studybadge.participation.domain.entity.Participation;
import com.tenten.studybadge.participation.domain.repository.ParticipationRepository;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.channel.dto.*;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.participation.ParticipationStatus;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyChannelService {

    private final StudyChannelRepository studyChannelRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MemberRepository memberRepository;
    private final ParticipationRepository participationRepository;
    private final NotificationSchedulerService notificationSchedulerService;

    @Transactional
    public Long create(StudyChannelCreateRequest request, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);

        StudyChannel studyChannel = request.toEntity();

        if (studyChannel.isStartDateBeforeTo(LocalDate.now(Clock.systemDefaultZone()))) {
            throw new InvalidStudyStartDateException();
        }

        StudyMember studyMember = StudyMember.leader(member, studyChannel);

        studyChannelRepository.save(studyChannel);
        studyMemberRepository.save(studyMember);

        return studyChannel.getId();
    }

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
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);
        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        }
        return studyChannel.toResponse(member);
    }

    public void startRecruitment(Long studyChannelId, Long memberId) {
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);

        checkLeader(studyChannel, member);
        studyChannel.startRecruitment();

        studyChannelRepository.save(studyChannel);
    }

    public void closeRecruitment(Long studyChannelId, Long memberId) {
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);

        checkLeader(studyChannel, member);
        studyChannel.closeRecruitment();

        List<Participation> participationList = participationRepository.findByStudyChannelId(studyChannelId);
        List<Participation> approveWaitingParticipationList = participationList.stream()
                .filter(participation -> participation.getParticipationStatus().equals(ParticipationStatus.APPROVE_WAITING))
                .toList();
        approveWaitingParticipationList.forEach(Participation::reject);

        studyChannelRepository.save(studyChannel);
        participationRepository.saveAll(approveWaitingParticipationList);

        notificationSchedulerService.scheduleStudyEndNotifications(studyChannel);
    }

    private void checkLeader(StudyChannel studyChannel, Member member) {
        if (!studyChannel.isLeader(member)) {
            throw new NotStudyLeaderException();
        }
    }

    public void editStudyChannel(Long studyChannelId, Long memberId, StudyChannelEditRequest studyChannelEditRequest) {
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);
        if (!studyChannel.isLeader(member)) {
            throw new NotStudyLeaderException();
        }
        studyChannel.edit(studyChannelEditRequest);
        studyChannelRepository.save(studyChannel);
    }

    public static class StudyChannelSpecification {

        public static Specification<StudyChannel> withRecruitmentStatus(String recruitmentStatus) {
            return (root, query, cb) -> cb.equal(root.get("recruitment").get("recruitmentStatus"), recruitmentStatus);
        }

        public static Specification<StudyChannel> withCategory(String category) {
            return (root, query, cb) -> cb.equal(root.get("category"), category);
        }

        public static Specification<StudyChannel> withMeetingType(String meetingType) {
            return (root, query, cb) -> cb.equal(root.get("meetingType"), meetingType);
        }

    }
}
