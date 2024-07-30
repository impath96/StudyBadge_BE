package com.tenten.studybadge.participation.service;

import com.tenten.studybadge.common.exception.participation.*;
import com.tenten.studybadge.common.exception.studychannel.AlreadyStudyMemberException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.common.exception.studychannel.RecruitmentCompletedStudyChannelException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.participation.domain.entity.Participation;
import com.tenten.studybadge.participation.domain.repository.ParticipationRepository;
import com.tenten.studybadge.participation.dto.StudyChannelParticipationStatusResponse;
import com.tenten.studybadge.point.domain.entity.Point;
import com.tenten.studybadge.point.domain.repository.PointRepository;
import com.tenten.studybadge.study.channel.domain.entity.Recruitment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.deposit.domain.entity.StudyChannelDeposit;
import com.tenten.studybadge.study.deposit.domain.repository.StudyChannelDepositRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.member.BadgeLevel;
import com.tenten.studybadge.type.participation.ParticipationStatus;
import com.tenten.studybadge.type.point.PointHistoryType;
import com.tenten.studybadge.type.point.TransferType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import com.tenten.studybadge.type.study.deposit.DepositStatus;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyChannelParticipationServiceTest {

    @InjectMocks
    private StudyChannelParticipationService studyChannelParticipationService;

    @Mock
    private StudyChannelRepository studyChannelRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private StudyChannelDepositRepository studyChannelDepositRepository;

    @DisplayName("[스터디 채널 참가 신청 테스트]")
    @Nested
    class ApplyStudyChannelParticipationTest {

        @DisplayName("정상적으로 스터디 채널에 참가 신청을 한다.")
        @Test
        void success_applyStudyChannelParticipation() {

            // given
            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .name("스터디명")
                    .description("스터디 설명")
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING)
                            .build())
                    .build();
            Member member = Member.builder().id(1L).build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(studyChannelRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(studyChannel));
            given(participationRepository.existsByMemberIdAndStudyChannelId(anyLong(), anyLong())).willReturn(false);

            // when
            studyChannelParticipationService.apply(studyChannel.getId(), 1L);

            // then
            ArgumentCaptor<Participation> participationCaptor = ArgumentCaptor.forClass(Participation.class);
            verify(participationRepository, times(1)).save(participationCaptor.capture());

            Participation participation = participationCaptor.getValue();

            assertThat(participation).isNotNull();
            assertThat(participation.getStudyChannel().getId()).isEqualTo(studyChannel.getId());
            assertThat(participation.getParticipationStatus()).isEqualTo(ParticipationStatus.APPROVE_WAITING);

        }

        @DisplayName("존재하지 않는 스터디 채널일 경우 참가 신청을 할 수 없다.")
        @Test
        void fail_notFoundStudyChannel() {

            // given
            Member member = mock(Member.class);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(studyChannelRepository.findByIdWithMember(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> studyChannelParticipationService.apply(1L, 1L))
                    .isExactlyInstanceOf(NotFoundStudyChannelException.class);
        }

        @DisplayName("이미 참가 신청을 한 상태일 경우 참가 신청을 할 수 없다.")
        @Test
        void fail_alreadyApplyParticipation() {

            // given
            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .name("스터디명")
                    .description("스터디 설명")
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING).
                            build())
                    .build();
            Member member = mock(Member.class);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(studyChannelRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(studyChannel));
            given(participationRepository.existsByMemberIdAndStudyChannelId(anyLong(), anyLong())).willReturn(true);

            // when
            assertThatThrownBy(() -> studyChannelParticipationService.apply(studyChannel.getId(), 1L))
                    .isExactlyInstanceOf(AlreadyAppliedParticipationException.class);
        }

        @DisplayName("해당 스터디 멤버일 경우 참가 신청을 할 수 없다.")
        @Test
        void fail_alreadyStudyMember() {

            // given
            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .name("스터디명")
                    .description("스터디 설명")
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING).
                            build())
                    .build();
            Member member = Member.builder()
                    .id(1L)
                    .email("이메일")
                    .name("김민호")
                    .build();
            StudyMember studyMember = StudyMember.builder()
                    .id(1L)
                    .studyChannel(studyChannel)
                    .member(member)
                    .studyMemberRole(StudyMemberRole.STUDY_MEMBER)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(studyChannelRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(studyChannel));

            // when & then
            assertThatThrownBy(() -> studyChannelParticipationService.apply(studyChannel.getId(), 1L))
                    .isExactlyInstanceOf(AlreadyStudyMemberException.class);
        }

        @DisplayName("모집이 마감된 스터디 채널은 참가 신청을 할 수 없다.")
        @Test
        void fail_recruitmentCompleted() {

            // given
            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .name("스터디명")
                    .description("스터디 설명")
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUIT_COMPLETED)
                            .build())
                    .build();
            Member member = mock(Member.class);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(studyChannelRepository.findByIdWithMember(anyLong())).willReturn(Optional.of(studyChannel));

            // when & then
            assertThatThrownBy(() -> studyChannelParticipationService.apply(studyChannel.getId(), 1L))
                    .isExactlyInstanceOf(RecruitmentCompletedStudyChannelException.class);
        }

    }

    @DisplayName("스터디 채널 참가 취소 테스트]")
    @Nested
    class CancelStudyChannelParticipationTest {

        @DisplayName("정상적으로 참가 신청을 취소한다.")
        @Test
        void success_cancelStudyChannelParticipation() {

            //given
            Member member = Member.builder().id(1L).build();
            Participation participation = Participation.builder()
                    .id(1L)
                    .member(member)
                    .participationStatus(ParticipationStatus.APPROVE_WAITING)
                    .build();
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(participationRepository.findById(1L)).willReturn(Optional.of(participation));

            //when
            studyChannelParticipationService.cancel(1L, 1L);

            //then
            assertThat(participation.getParticipationStatus()).isEqualTo(ParticipationStatus.CANCELED);
        }

        @DisplayName("다른 회원의 참가 신청을 취소하려는 경우 예외가 발생한다.")
        @Test
        void fail_cancelOtherMemberParticipation() {

            //given
            Member member = Member.builder().id(1L).build();
            Member other = Member.builder().id(2L).build();
            Participation participation = Participation.builder()
                    .id(1L)
                    .member(member)
                    .participationStatus(ParticipationStatus.APPROVE_WAITING)
                    .build();
            given(memberRepository.findById(2L)).willReturn(Optional.of(other));
            given(participationRepository.findById(1L)).willReturn(Optional.of(participation));

            //when & then
            Assertions.assertThatThrownBy(() -> studyChannelParticipationService.cancel(1L, 2L))
                    .isExactlyInstanceOf(OtherMemberParticipationCancelException.class);
        }

    }

    @DisplayName("[스터디 채널 참가 신청 승인 테스트]")
    @Nested
    class approveStudyChannelParticipationTest {

        @DisplayName("정상적으로 스터디 채널 신청을 승인한다.")
        @Test
        void success_approveStudyChannelParticipation() {

            //given
            Member member = Member.builder().id(1L).point(30_000).build();
            Member leader = Member.builder().id(2L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .deposit(10_000)
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .member(leader)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);

            Participation participation = Participation.builder()
                    .member(member)
                    .studyChannel(studyChannel)
                    .participationStatus(ParticipationStatus.APPROVE_WAITING)
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(leader));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));
            given(participationRepository.findByIdWithMember(1L)).willReturn(Optional.of(participation));

            //when
            studyChannelParticipationService.approve(1L, 1L, 2L);

            ArgumentCaptor<StudyMember> studyMemberCaptor = ArgumentCaptor.forClass(StudyMember.class);
            ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            ArgumentCaptor<StudyChannelDeposit> studyChannelDepositCaptor = ArgumentCaptor.forClass(StudyChannelDeposit.class);

            verify(studyMemberRepository, times(1)).save(studyMemberCaptor.capture());
            verify(pointRepository, times(1)).save(pointCaptor.capture());
            verify(memberRepository, times(1)).save(memberCaptor.capture());
            verify(studyChannelDepositRepository, times(1)).save(studyChannelDepositCaptor.capture());

            StudyMember studyMemberCaptorValue = studyMemberCaptor.getValue();
            Point pointCaptorValue = pointCaptor.getValue();
            Member memberCaptorValue = memberCaptor.getValue();
            StudyChannelDeposit studyChannelDepositValue = studyChannelDepositCaptor.getValue();

            //then
            assertThat(participation.getParticipationStatus()).isEqualTo(ParticipationStatus.APPROVED);
            assertThat(studyMemberCaptorValue.getStudyChannel().getId()).isEqualTo(1L);
            assertThat(studyMemberCaptorValue.getMember().getId()).isEqualTo(1L);
            assertThat(studyMemberCaptorValue.getStudyMemberRole()).isEqualTo(StudyMemberRole.STUDY_MEMBER);

            assertThat(pointCaptorValue.getMember().getId()).isEqualTo(1L);
            assertThat(pointCaptorValue.getAmount()).isEqualTo(-10_000);
            assertThat(pointCaptorValue.getHistoryType()).isEqualTo(PointHistoryType.SPENT);
            assertThat(pointCaptorValue.getTransferType()).isEqualTo(TransferType.STUDY_DEPOSIT);

            assertThat(memberCaptorValue.getId()).isEqualTo(1L);
            assertThat(memberCaptorValue.getPoint()).isEqualTo(20_000);

            assertThat(studyChannelDepositValue.getStudyChannel().getId()).isEqualTo(1L);
            assertThat(studyChannelDepositValue.getMember().getId()).isEqualTo(1L);
            assertThat(studyChannelDepositValue.getDepositStatus()).isEqualTo(DepositStatus.DEPOSIT);
            assertThat(studyChannelDepositValue.getAmount()).isEqualTo(10_000);

        }

        @DisplayName("다른 스터디 채널의 참가 신청일 경우 예외")
        @Test
        void fail_otherStudyChannelParticipationException() {

            //given
            Member member = Member.builder().id(1L).build();
            Member leader = Member.builder().id(2L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .member(leader)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);

            Participation participation = Participation.builder()
                    .member(member)
                    .studyChannel(studyChannel)
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(leader));
            given(participationRepository.findByIdWithMember(1L)).willReturn(Optional.of(participation));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            //when & then
            assertThatThrownBy(() -> studyChannelParticipationService.approve(2L, 1L, 2L))
                    .isExactlyInstanceOf(OtherStudyChannelParticipationException.class);

        }

        @DisplayName("승인은 해당 스터디 채널의 리더만 가능하다.")
        @Test
        void fail_notAuthorizedApprovalException() {

            //given
            Member member = Member.builder().id(1L).build();
            Member member2 = Member.builder().id(2L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyMemberRole(StudyMemberRole.STUDY_MEMBER)
                    .member(member2)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);

            Participation participation = Participation.builder()
                    .member(member)
                    .studyChannel(studyChannel)
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(member2));
            given(participationRepository.findByIdWithMember(1L)).willReturn(Optional.of(participation));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            //when & then
            assertThatThrownBy(() -> studyChannelParticipationService.approve(1L, 1L, 2L))
                    .isExactlyInstanceOf(NotAuthorizedApprovalException.class);

        }

        @DisplayName("참가 신청 상태가 승인 대기 상태가 아닐 경우 예외가 발생한다.")
        @Test
        void fail_invalidApprovalStatusException() {

            //given
            Member member = Member.builder().id(1L).build();
            Member leader = Member.builder().id(2L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .member(leader)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);

            Participation participation = Participation.builder()
                    .member(member)
                    .studyChannel(studyChannel)
                    .participationStatus(ParticipationStatus.CANCELED)
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(leader));
            given(participationRepository.findByIdWithMember(1L)).willReturn(Optional.of(participation));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            //when & then
            assertThatThrownBy(() -> studyChannelParticipationService.approve(1L, 1L, 2L))
                    .isExactlyInstanceOf(InvalidApprovalStatusException.class);

        }

    }

    @DisplayName("[스터디 채널 참가 신청 거절 테스트]")
    @Nested
    class rejectStudyChannelParticipationTest {

        @DisplayName("정상적으로 스터디 채널 신청을 거절한다.")
        @Test
        void success_approveStudyChannelParticipation() {

            //given
            Member member = Member.builder().id(1L).build();
            Member leader = Member.builder().id(2L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .member(leader)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);

            Participation participation = Participation.builder()
                    .member(member)
                    .studyChannel(studyChannel)
                    .participationStatus(ParticipationStatus.APPROVE_WAITING)
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(leader));
            given(participationRepository.findById(1L)).willReturn(Optional.of(participation));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            //when
            studyChannelParticipationService.reject(1L, 1L, 2L);

            //then
            assertThat(participation.getParticipationStatus()).isEqualTo(ParticipationStatus.REJECTED);

        }

        @DisplayName("다른 스터디 채널의 참가 신청일 경우 예외")
        @Test
        void fail_otherStudyChannelParticipationException() {

            //given
            Member member = Member.builder().id(1L).build();
            Member leader = Member.builder().id(2L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .member(leader)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);

            Participation participation = Participation.builder()
                    .member(member)
                    .studyChannel(studyChannel)
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(leader));
            given(participationRepository.findById(1L)).willReturn(Optional.of(participation));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            //when & then
            assertThatThrownBy(() -> studyChannelParticipationService.reject(2L, 1L, 2L))
                    .isExactlyInstanceOf(OtherStudyChannelParticipationException.class);

        }

        @DisplayName("거절은 해당 스터디 채널의 리더만 가능하다.")
        @Test
        void fail_notAuthorizedApprovalException() {

            //given
            Member member = Member.builder().id(1L).build();
            Member member2 = Member.builder().id(2L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyMemberRole(StudyMemberRole.STUDY_MEMBER)
                    .member(member2)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);

            Participation participation = Participation.builder()
                    .member(member)
                    .studyChannel(studyChannel)
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(member2));
            given(participationRepository.findById(1L)).willReturn(Optional.of(participation));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            //when & then
            assertThatThrownBy(() -> studyChannelParticipationService.reject(1L, 1L, 2L))
                    .isExactlyInstanceOf(NotAuthorizedRejectException.class);

        }

        @DisplayName("참가 신청 상태가 승인 대기 상태가 아닐 경우 예외가 발생한다.")
        @Test
        void fail_invalidApprovalStatusException() {

            //given
            Member member = Member.builder().id(1L).build();
            Member leader = Member.builder().id(2L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .member(leader)
                    .build();
            studyChannel.getStudyMembers().add(studyMember);

            Participation participation = Participation.builder()
                    .member(member)
                    .studyChannel(studyChannel)
                    .participationStatus(ParticipationStatus.CANCELED)
                    .build();

            given(memberRepository.findById(2L)).willReturn(Optional.of(leader));
            given(participationRepository.findById(1L)).willReturn(Optional.of(participation));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            //when & then
            assertThatThrownBy(() -> studyChannelParticipationService.reject(1L, 1L, 2L))
                    .isExactlyInstanceOf(InvalidApprovalStatusException.class);

        }

    }

    @DisplayName("[스터디 채널 참가 신청자 조회 테스트]")
    @Nested
    class GetStudyChannelParticipantsTest {

        @DisplayName("현재 스터디 채널이 모집 마감 상태일 경우 모집 마감 상태와 빈 참가자 목록이 반환된다.")
        @Test
        void success_getRecruitmentCompletedStudyChannelParticipation() {
            //given
            Member member1 = Member.builder().id(1L).build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .recruitment(Recruitment.builder()
                            .recruitmentStatus(RecruitmentStatus.RECRUIT_COMPLETED)
                            .recruitmentNumber(7)
                            .build())
                    .build();

            StudyMember leader = StudyMember.builder()
                    .id(1L)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .member(member1)
                    .build();
            studyChannel.getStudyMembers().add(leader);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member1));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            //when
            StudyChannelParticipationStatusResponse response = studyChannelParticipationService.getParticipationStatus(1L, 1L);

            //then
            assertThat(response.getStudyChannelId()).isEqualTo(1L);
            assertThat(response.getRecruitmentStatus()).isEqualTo(RecruitmentStatus.RECRUIT_COMPLETED);
            assertThat(response.getParticipants().size()).isEqualTo(0);
        }

        @DisplayName("현재 스터디 채널이 모집 중일 경우 신청자 정보와 모집 중 상태를 반환한다.")
        @Test
        void success_getRecruitingStudyChannelParticipation() {
            //given
            Member member1 = Member.builder().id(1L).build();
            Member member2 = Member.builder()
                    .id(2L)
                    .name("회원1")
                    .banCnt(2)
                    .imgUrl("imageUrl")
                    .badgeLevel(BadgeLevel.SILVER)
                    .build();

            StudyChannel studyChannel = StudyChannel.builder()
                    .id(1L)
                    .recruitment(Recruitment.builder()
                            .recruitmentStatus(RecruitmentStatus.RECRUITING)
                            .recruitmentNumber(7)
                            .build())
                    .build();

            StudyMember leader = StudyMember.builder()
                    .id(1L)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .member(member1)
                    .build();

            studyChannel.getStudyMembers().add(leader);

            Participation participation = Participation.builder()
                    .id(1L)
                    .participationStatus(ParticipationStatus.APPROVE_WAITING)
                    .studyChannel(studyChannel)
                    .member(member2)
                    .build();

            given(memberRepository.findById(1L)).willReturn(Optional.of(member1));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));
            given(participationRepository.findByStudyChannelIdWithMember(1L)).willReturn(List.of(participation));

            //when
            StudyChannelParticipationStatusResponse response = studyChannelParticipationService.getParticipationStatus(1L, 1L);

            //then
            assertThat(response.getStudyChannelId()).isEqualTo(1L);
            assertThat(response.getRecruitmentStatus()).isEqualTo(RecruitmentStatus.RECRUITING);
            assertThat(response.getParticipants().size()).isEqualTo(1);
            assertThat(response.getParticipants().get(0).getParticipationId()).isEqualTo(1L);
            assertThat(response.getParticipants().get(0).getMemberId()).isEqualTo(2L);
            assertThat(response.getParticipants().get(0).getName()).isEqualTo("회원1");
            assertThat(response.getParticipants().get(0).getBanCnt()).isEqualTo(2);
            assertThat(response.getParticipants().get(0).getImageUrl()).isEqualTo("imageUrl");
            assertThat(response.getParticipants().get(0).getBadgeLevel()).isEqualTo(BadgeLevel.SILVER);
        }
    }

}