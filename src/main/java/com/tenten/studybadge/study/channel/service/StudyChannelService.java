package com.tenten.studybadge.study.channel.service;

import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.payment.NotEnoughPointException;
import com.tenten.studybadge.common.exception.studychannel.InvalidStudyStartDateException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.common.exception.studychannel.NotStudyLeaderException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.notification.service.NotificationSchedulerService;
import com.tenten.studybadge.participation.domain.entity.Participation;
import com.tenten.studybadge.participation.domain.repository.ParticipationRepository;
import com.tenten.studybadge.point.domain.entity.Point;
import com.tenten.studybadge.point.domain.repository.PointRepository;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.channel.dto.StudyChannelCreateRequest;
import com.tenten.studybadge.study.channel.dto.StudyChannelEditRequest;
import com.tenten.studybadge.study.deposit.domain.entity.StudyChannelDeposit;
import com.tenten.studybadge.study.deposit.domain.repository.StudyChannelDepositRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.participation.ParticipationStatus;
import com.tenten.studybadge.type.point.PointHistoryType;
import com.tenten.studybadge.type.point.TransferType;
import com.tenten.studybadge.type.study.deposit.DepositStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyChannelService {

    private final StudyChannelRepository studyChannelRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MemberRepository memberRepository;
    private final ParticipationRepository participationRepository;
    private final NotificationSchedulerService notificationSchedulerService;
    private final StudyChannelDepositRepository studyChannelDepositRepository;
    private final PointRepository pointRepository;

    @Transactional
    public Long create(StudyChannelCreateRequest request, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);

        StudyChannel studyChannel = request.toEntity();

        if (studyChannel.isStartDateBeforeTo(LocalDate.now(Clock.systemDefaultZone()))) {
            throw new InvalidStudyStartDateException();
        }
        if (member.getPoint() < request.getDeposit()) {
            throw new NotEnoughPointException();
        }
        StudyMember studyMember = StudyMember.leader(member, studyChannel);

        studyChannelRepository.save(studyChannel);
        studyMemberRepository.save(studyMember);

        Point point = deductPoint(member, studyChannel.getDeposit());
        recordDeposit(studyChannel, member, studyMember, -1 * point.getAmount());

        return studyChannel.getId();
    }

    public boolean checkStudyMemberInStudyChannel(Long memberId, Long studyChannelId) {
        Integer result = studyMemberRepository.existsByMemberIdAndStudyChannelIdAndStudyMemberStatus(memberId, studyChannelId);
        return result != null && result == 1;
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

    private void recordDeposit(StudyChannel channel, Member member, StudyMember studyMember, Integer amount) {
        StudyChannelDeposit deposit = StudyChannelDeposit.builder()
                .depositAt(LocalDateTime.now())
                .amount(amount)
                .refundsAmount(0)
                .depositStatus(DepositStatus.DEPOSIT)
                .studyChannel(channel)
                .member(member)
                .attendanceRatio(0.0)
                .studyMember(studyMember)
                .build();
        studyChannelDepositRepository.save(deposit);
    }

    private Point deductPoint(Member member, Integer deposit) {

        Point point = Point.builder()
                .amount(-1 * deposit)
                .member(member)
                .historyType(PointHistoryType.SPENT)
                .transferType(TransferType.STUDY_DEPOSIT)
                .build();

        Member updatedMember = member.toBuilder()
                .point(member.getPoint() + point.getAmount())
                .build();
        pointRepository.save(point);
        memberRepository.save(updatedMember);
        return point;
    }

}
