package com.tenten.studybadge.participation.service;

import com.tenten.studybadge.common.exception.participation.AlreadyAppliedParticipationException;
import com.tenten.studybadge.common.exception.participation.NotAuthorizedApprovalException;
import com.tenten.studybadge.common.exception.participation.OtherMemberParticipationCancelException;
import com.tenten.studybadge.common.exception.participation.OtherStudyChannelParticipationException;
import com.tenten.studybadge.common.exception.studychannel.AlreadyStudyMemberException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.common.exception.studychannel.RecruitmentCompletedStudyChannelException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.participation.domain.entity.Participation;
import com.tenten.studybadge.participation.domain.repository.ParticipationRepository;
import com.tenten.studybadge.study.channel.domain.entity.Recruitment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.type.participation.ParticipationStatus;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
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
            given(studyChannelRepository.findById(anyLong())).willReturn(Optional.of(studyChannel));
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
            given(studyChannelRepository.findById(anyLong())).willReturn(Optional.empty());

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
            given(studyChannelRepository.findById(anyLong())).willReturn(Optional.of(studyChannel));
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
            given(studyChannelRepository.findById(anyLong())).willReturn(Optional.of(studyChannel));

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
            given(studyChannelRepository.findById(anyLong())).willReturn(Optional.of(studyChannel));

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

            //when
            studyChannelParticipationService.approve(1L, 1L, 2L);

            //then
            assertThat(participation.getParticipationStatus()).isEqualTo(ParticipationStatus.APPROVED);
            assertThat(studyChannel.getStudyMembers().size()).isEqualTo(2);

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
            given(participationRepository.findById(1L)).willReturn(Optional.of(participation));

            //when & then
            assertThatThrownBy(() -> studyChannelParticipationService.approve(1L, 1L, 2L))
                    .isExactlyInstanceOf(NotAuthorizedApprovalException.class);

        }



    }

}