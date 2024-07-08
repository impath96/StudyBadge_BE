package com.tenten.studybadge.participation.service;

import com.tenten.studybadge.common.exception.participation.AlreadyAppliedParticipationException;
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
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.participation.ParticipationStatus;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class StudyChannelParticipationServiceTest {

    @Autowired
    private StudyChannelParticipationService studyChannelParticipationService;

    @Autowired
    private StudyChannelRepository studyChannelRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void beforeEach() {
        studyMemberRepository.deleteAll();
        participationRepository.deleteAll();
        studyChannelRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @DisplayName("[스터디 채널 참가 신청 테스트]")
    @Nested
    class ApplyStudyChannelParticipationTest {

        @DisplayName("정상적으로 스터디 채널에 참가 신청을 한다.")
        @Test
        void success_applyStudyChannelParticipation() {

            StudyChannel studyChannel = StudyChannel.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING)
                            .build())
                    .build();
            studyChannelRepository.save(studyChannel);

            studyChannelParticipationService.apply(studyChannel.getId(), 1L);

            Participation participation = participationRepository.findAll().stream().findFirst().orElseThrow();

            assertThat(participation).isNotNull();
            assertThat(participation.getStudyChannel().getId()).isEqualTo(studyChannel.getId());
            assertThat(participation.getParticipationStatus()).isEqualTo(ParticipationStatus.APPROVE_WAITING);

        }

        @DisplayName("존재하지 않는 스터디 채널일 경우 참가 신청을 할 수 없다.")
        @Test
        void fail_notFoundStudyChannel() {
            assertThatThrownBy(() -> studyChannelParticipationService.apply(1L, 1L))
                    .isExactlyInstanceOf(NotFoundStudyChannelException.class);
        }

        @DisplayName("이미 참가 신청을 한 상태일 경우 참가 신청을 할 수 없다.")
        @Test
        void fail_alreadyApplyParticipation() {

            StudyChannel studyChannel = StudyChannel.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING).
                            build())
                    .build();
            Participation participation = Participation.builder()
                    .studyChannel(studyChannel)
                    .memberId(1L)
                    .participationStatus(ParticipationStatus.APPROVE_WAITING)
                    .build();
            studyChannelRepository.save(studyChannel);
            participationRepository.save(participation);

            assertThatThrownBy(() -> studyChannelParticipationService.apply(studyChannel.getId(), 1L))
                    .isExactlyInstanceOf(AlreadyAppliedParticipationException.class);
        }

        @DisplayName("해당 스터디 멤버일 경우 참가 신청을 할 수 없다.")
        @Test
        void fail_alreadyStudyMember() {

            StudyChannel studyChannel = StudyChannel.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING).
                            build())
                    .build();
            Member member = Member.builder()
                    .email("이메일")
                    .name("김민호")
                    .build();
            StudyMember studyMember = StudyMember.builder()
                    .studyChannel(studyChannel)
                    .member(member)
                    .studyMemberRole(StudyMemberRole.STUDY_MEMBER)
                    .build();

            studyChannelRepository.save(studyChannel);
            memberRepository.save(member);
            studyMemberRepository.save(studyMember);

            assertThatThrownBy(() -> studyChannelParticipationService.apply(studyChannel.getId(), 1L))
                    .isExactlyInstanceOf(AlreadyStudyMemberException.class);
        }

        @DisplayName("모집이 마감된 스터디 채널은 참가 신청을 할 수 없다.")
        @Test
        void fail_recruitmentCompleted() {

            StudyChannel studyChannel = StudyChannel.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUIT_COMPLETED)
                            .build())
                    .build();
            studyChannelRepository.save(studyChannel);

            assertThatThrownBy(() -> studyChannelParticipationService.apply(studyChannel.getId(), 1L))
                    .isExactlyInstanceOf(RecruitmentCompletedStudyChannelException.class);
        }

    }

}