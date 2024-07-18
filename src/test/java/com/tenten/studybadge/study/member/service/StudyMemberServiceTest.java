package com.tenten.studybadge.study.member.service;

import com.tenten.studybadge.common.exception.studychannel.AlreadyExistsSubLeaderException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.study.channel.domain.entity.Recruitment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.entity.StudyDuration;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.study.member.dto.StudyMembersResponse;
import com.tenten.studybadge.type.member.BadgeLevel;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudyMemberServiceTest {

    @InjectMocks
    private StudyMemberService studyMemberService;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StudyChannelRepository studyChannelRepository;

    @DisplayName("[스터디 멤버 리스트 조회 테스트]")
    @Nested
    class GetStudyMembersTest {

        Member member1;
        Member member2;

        @BeforeEach
        void setUp() {
            member1 = Member.builder()
                    .id(1L)
                    .name("회원1")
                    .banCnt(2)
                    .imgUrl("imageUrl1")
                    .badgeLevel(BadgeLevel.SILVER)
                    .build();
            member2 = Member.builder()
                    .id(2L)
                    .name("회원2")
                    .banCnt(2)
                    .imgUrl("imageUrl2")
                    .badgeLevel(BadgeLevel.NONE)
                    .build();

        }

        @DisplayName("리더가 스터디 멤버를 조회할 경우 isLeader는 True")
        @Test
        void success_getStudyMembersForLeader() {
            StudyChannel studyChannel = StudyChannel.builder().id(1L).build();

            StudyMember leader = StudyMember.leader(member1, studyChannel);
            StudyMember studyMember = StudyMember.member(member2, studyChannel);
            List<StudyMember> studyMembers = List.of(leader, studyMember);

            given(studyMemberRepository.existsByStudyChannelIdAndMemberId(1L, 1L))
                    .willReturn(true);

            given(studyMemberRepository.findAllByStudyChannelIdWithMember(1L)).willReturn(studyMembers);

            StudyMembersResponse response = studyMemberService.getStudyMembers(1L, 1L);

            assertThat(response.isLeader()).isTrue();
            assertThat(response.getStudyMembers().size()).isEqualTo(2);
            assertThat(response.getStudyMembers().get(0).getMemberId()).isEqualTo(1L);
            assertThat(response.getStudyMembers().get(0).getImageUrl()).isEqualTo("imageUrl1");
            assertThat(response.getStudyMembers().get(0).getName()).isEqualTo("회원1");
            assertThat(response.getStudyMembers().get(0).getRole()).isEqualTo(StudyMemberRole.LEADER);
            assertThat(response.getStudyMembers().get(0).getBadgeLevel()).isEqualTo(BadgeLevel.SILVER);
            assertThat(response.getStudyMembers().get(1).getMemberId()).isEqualTo(2L);
            assertThat(response.getStudyMembers().get(1).getImageUrl()).isEqualTo("imageUrl2");
            assertThat(response.getStudyMembers().get(1).getName()).isEqualTo("회원2");
            assertThat(response.getStudyMembers().get(1).getRole()).isEqualTo(StudyMemberRole.STUDY_MEMBER);
            assertThat(response.getStudyMembers().get(1).getBadgeLevel()).isEqualTo(BadgeLevel.NONE);

        }

        @DisplayName("스터디 멤버가 스터디 멤버를 조회할 경우 isLeader는 False")
        @Test
        void success_getStudyMembersForStudyMember() {
            StudyChannel studyChannel = StudyChannel.builder().id(1L).build();

            StudyMember leader = StudyMember.leader(member1, studyChannel);
            StudyMember studyMember = StudyMember.member(member2, studyChannel);
            List<StudyMember> studyMembers = List.of(leader, studyMember);

            given(studyMemberRepository.existsByStudyChannelIdAndMemberId(1L, 2L))
                    .willReturn(true);

            given(studyMemberRepository.findAllByStudyChannelIdWithMember(1L)).willReturn(studyMembers);

            StudyMembersResponse response = studyMemberService.getStudyMembers(1L, 2L);

            assertThat(response.isLeader()).isFalse();
            assertThat(response.getStudyMembers().size()).isEqualTo(2);
            assertThat(response.getStudyMembers().get(0).getMemberId()).isEqualTo(1L);
            assertThat(response.getStudyMembers().get(0).getImageUrl()).isEqualTo("imageUrl1");
            assertThat(response.getStudyMembers().get(0).getName()).isEqualTo("회원1");
            assertThat(response.getStudyMembers().get(0).getRole()).isEqualTo(StudyMemberRole.LEADER);
            assertThat(response.getStudyMembers().get(0).getBadgeLevel()).isEqualTo(BadgeLevel.SILVER);
            assertThat(response.getStudyMembers().get(1).getMemberId()).isEqualTo(2L);
            assertThat(response.getStudyMembers().get(1).getImageUrl()).isEqualTo("imageUrl2");
            assertThat(response.getStudyMembers().get(1).getName()).isEqualTo("회원2");
            assertThat(response.getStudyMembers().get(1).getRole()).isEqualTo(StudyMemberRole.STUDY_MEMBER);
            assertThat(response.getStudyMembers().get(1).getBadgeLevel()).isEqualTo(BadgeLevel.NONE);


        }

    }

    @DisplayName("[스터디 채널 서브 리더 권한 부여 기능 테스트]")
    @Nested
    class AssignSubLeaderRoleTest {

        Member member1;
        Member member2;
        Member member3;

        StudyChannel studyChannel;

        @BeforeEach
        void setUp() {
            member1 = Member.builder().id(1L).name("회원 1").build();
            member2 = Member.builder().id(2L).name("회원 2").build();
            member3 = Member.builder().id(3L).name("회원 3").build();

            LocalDate now = LocalDate.now();
            studyChannel = StudyChannel.builder()
                    .id(1L)
                    .name("스터디명")
                    .description("스터디 설명")
                    .studyDuration(StudyDuration.builder()
                            .studyStartDate(now.plusDays(2))
                            .studyEndDate(now.plusMonths(4))
                            .build())
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(6)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING)
                            .build())
                    .category(Category.IT)
                    .region(null)
                    .meetingType(MeetingType.ONLINE)
                    .chattingUrl("오픈채팅방 URL")
                    .deposit(10_000)
                    .viewCnt(4)
                    .build();
        }

        @DisplayName("정상적으로 서브 리더의 권한을 부여한다.")
        @Test
        void success_assignSubLeaderRole() {
            StudyMember leader = StudyMember.builder()
                    .id(1L)
                    .studyChannel(studyChannel)
                    .member(member1)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .build();
            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyChannel(studyChannel)
                    .member(member2)
                    .studyMemberRole(StudyMemberRole.STUDY_MEMBER)
                    .build();

            studyChannel.getStudyMembers().addAll(List.of(leader, studyMember));

            given(memberRepository.findById(1L)).willReturn(Optional.of(member1));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            studyMemberService.assignStudyLeaderRole(1L, 1L, 2L);

            assertThat(studyMember.getStudyMemberRole()).isEqualTo(StudyMemberRole.SUB_LEADER);
        }

        @DisplayName("스터디 채널 내에 서브 리더가 있을 경우 예외가 발생한다.")
        @Test
        void fail_alreadyExistsSubLeader() {
            StudyMember leader = StudyMember.builder()
                    .id(1L)
                    .studyChannel(studyChannel)
                    .member(member1)
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .build();
            StudyMember studyMember = StudyMember.builder()
                    .id(2L)
                    .studyChannel(studyChannel)
                    .member(member2)
                    .studyMemberRole(StudyMemberRole.SUB_LEADER)
                    .build();

            studyChannel.getStudyMembers().addAll(List.of(leader, studyMember));

            given(memberRepository.findById(1L)).willReturn(Optional.of(member1));
            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));

            assertThatThrownBy(() ->
                studyMemberService.assignStudyLeaderRole(1L, 1L, 2L)
            )
            .isExactlyInstanceOf(AlreadyExistsSubLeaderException.class);
        }
    }

}