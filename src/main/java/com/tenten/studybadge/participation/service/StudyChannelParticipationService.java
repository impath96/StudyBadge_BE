package com.tenten.studybadge.participation.service;

import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.participation.*;
import com.tenten.studybadge.common.exception.studychannel.*;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.participation.domain.entity.Participation;
import com.tenten.studybadge.participation.domain.repository.ParticipationRepository;
import com.tenten.studybadge.participation.dto.StudyChannelParticipationStatusResponse;
import com.tenten.studybadge.point.domain.entity.Point;
import com.tenten.studybadge.point.domain.repository.PointRepository;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyChannelParticipationService {

    private final ParticipationRepository  participationRepository;
    private final StudyChannelRepository studyChannelRepository;
    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final StudyChannelDepositRepository studyChannelDepositRepository;
    private final StudyMemberRepository studyMemberRepository;

    // TODO 1) 탈퇴 당한 회원인가?
    //      2) 참가 거절 당한 회원인가?
    public void apply(Long studyChannelId, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);

        if (studyChannel.isStudyMember(memberId)) {
            throw new AlreadyStudyMemberException();
        }

        if (studyChannel.isRecruitmentCompleted()) {
            throw new RecruitmentCompletedStudyChannelException();
        }

        if (studyChannel.getStudyDuration().getStudyEndDate().isBefore(LocalDate.now())) {
            throw new EndStudyChannelException();
        }

        if (participationRepository.existsByMemberIdAndStudyChannelId(memberId, studyChannel.getId())) {
            throw new AlreadyAppliedParticipationException();
        }

        Participation participation = Participation.create(member, studyChannel);

        participationRepository.save(participation);

    }

    // TODO 이미 승인/거절된 참가 신청을 취소할 경우 예외 처리
    @Transactional
    public void cancel(Long participationId, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        Participation participation = participationRepository.findById(participationId).orElseThrow(NotFoundParticipationException::new);
        if (!participation.isCreatedBy(member)) {
            throw new OtherMemberParticipationCancelException();
        }
        participation.cancel();
    }

    public void approve(Long studyChannelId, Long participationId, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        Participation participation = participationRepository.findByIdWithMember(participationId).orElseThrow(NotFoundParticipationException::new);
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(participation.getStudyChannel().getId()).orElseThrow(NotFoundStudyChannelException::new);
        Member applyMember = participation.getMember();

        if (!studyChannel.getId().equals(studyChannelId)) {
             throw new OtherStudyChannelParticipationException();
        }
        if (!studyChannel.isLeader(member)){
            throw new NotAuthorizedApprovalException();
        }
        if (!participation.getParticipationStatus().equals(ParticipationStatus.APPROVE_WAITING)) {
            throw new InvalidApprovalStatusException();
        }
        if (studyChannel.isFull()) {
            throw new AlreadyStudyMemberFullException();
        }
        StudyMember studyMember = approveMember(participation, studyChannel, applyMember);
        Point point = deductPoint(applyMember, studyChannel.getDeposit());
        recordDeposit(studyChannel, applyMember, studyMember, -1 * point.getAmount());

    }

    public void reject(Long studyChannelId, Long participationId, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        Participation participation = participationRepository.findById(participationId).orElseThrow(NotFoundParticipationException::new);
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(participation.getStudyChannel().getId()).orElseThrow(NotFoundStudyChannelException::new);

        if (!studyChannel.getId().equals(studyChannelId)) {
            throw new OtherStudyChannelParticipationException();
        }
        if (!studyChannel.isLeader(member)){
            throw new NotAuthorizedRejectException();
        }
        if (!participation.getParticipationStatus().equals(ParticipationStatus.APPROVE_WAITING)) {
            throw new InvalidApprovalStatusException();
        }
        participation.reject();

        participationRepository.save(participation);
    }

    public StudyChannelParticipationStatusResponse getParticipationStatus(Long studyChannelId, Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);
        if (!studyChannel.isLeader(member)) {
            throw new NotStudyLeaderException();
        }

        List<Participation> participationList;
        if (studyChannel.isRecruitmentCompleted()){
            participationList = Collections.emptyList();
        } else {
            participationList = participationRepository.findByStudyChannelIdWithMember(studyChannel.getId());
        }

        return StudyChannelParticipationStatusResponse.builder()
                .studyChannelId(studyChannel.getId())
                .recruitmentStatus(studyChannel.getRecruitment().getRecruitmentStatus())
                .participants(participationList.stream()
                        .map(Participation::toResponse)
                        .toList())
                .build();
    }

    private StudyMember approveMember(Participation participation, StudyChannel studyChannel, Member member) {
        // 참가 신청 내역 변경(승인 대기중 -> 승인됨)
        participation.approve();
        StudyMember studyMember = StudyMember.member(member, studyChannel);
        participationRepository.save(participation);
        studyMemberRepository.save(studyMember);
        return studyMember;
    }

    // 예치금 내역 기록
    private void recordDeposit(StudyChannel channel, Member member, StudyMember studyMember, Integer amount) {
        StudyChannelDeposit deposit = StudyChannelDeposit.builder()
                .depositAt(LocalDateTime.now())
                .amount(amount)
                .refundsAmount(0)
                .depositStatus(DepositStatus.DEPOSIT)
                .studyChannel(channel)
                .member(member)
                .studyMember(studyMember)
                .attendanceRatio(0.0)
                .build();
        studyChannelDepositRepository.save(deposit);
    }

    // 포인트 차감
    private Point deductPoint(Member member, Integer deposit) {

        // 포인트 내역 기록
        Point point = Point.builder()
                .amount(-1 * deposit)
                .member(member)
                .historyType(PointHistoryType.SPENT)
                .transferType(TransferType.STUDY_DEPOSIT)
                .build();

        // 사용자 포인트 차감(차감 시 포인트 내역에 기록된 금액을 차감) - 포인트 내역에 금액이 마이너스(-) 인것 주의!
        Member updatedMember = member.toBuilder()
                .point(member.getPoint() + point.getAmount())
                .build();
        pointRepository.save(point);
        memberRepository.save(updatedMember);
        return point;
    }
}
