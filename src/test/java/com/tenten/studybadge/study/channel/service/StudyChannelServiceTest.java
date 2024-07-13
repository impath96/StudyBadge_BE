package com.tenten.studybadge.study.channel.service;

import com.tenten.studybadge.common.exception.studychannel.InvalidStudyDurationException;
import com.tenten.studybadge.common.exception.studychannel.InvalidStudyStartDateException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.study.channel.domain.entity.Recruitment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.entity.StudyDuration;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.channel.dto.SearchCondition;
import com.tenten.studybadge.study.channel.dto.StudyChannelCreateRequest;
import com.tenten.studybadge.study.channel.dto.StudyChannelListResponse;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyChannelServiceTest {

    @InjectMocks
    private StudyChannelService studyChannelService;

    @Mock
    private StudyChannelRepository studyChannelRepository;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @DisplayName("[스터디 채널 생성 테스트]")
    @Nested
    class CreateStudyChannelTest {

        private StudyChannelCreateRequest request;

        @DisplayName("스터디 채널을 생성한 회원은 채널의 스터디 멤버가 되고 역할은 리더가 된다.")
        @Test
        void success_createStudyChannel() {

            // given
            LocalDate now = LocalDate.now();
            StudyChannelCreateRequest request = StudyChannelCreateRequest.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .startDate(now.plusDays(3))
                    .endDate(now.plusMonths(3))
                    .recruitmentNumber(8)
                    .minRecruitmentNumber(4)
                    .category(Category.IT)
                    .region("")
                    .meetingType(MeetingType.ONLINE)
                    .chattingUrl("오픈채팅방 URL")
                    .deposit(10_000)
                    .depositDescription("스터디 채널 승인 시 자동으로 빠져나갑니다.")
                    .build();

            Member member = mock(Member.class);
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when
            studyChannelService.create(request, 1L);

            // then
            ArgumentCaptor<StudyChannel> studyChannelCaptor = ArgumentCaptor.forClass(StudyChannel.class);
            ArgumentCaptor<StudyMember> studyMemberCaptor = ArgumentCaptor.forClass(StudyMember.class);

            verify(studyChannelRepository, times(1)).save(studyChannelCaptor.capture());
            verify(studyMemberRepository, times(1)).save(studyMemberCaptor.capture());

            StudyChannel studyChannel = studyChannelCaptor.getValue();
            StudyMember studyMember = studyMemberCaptor.getValue();

            assertThat(studyChannel.getRecruitment().getRecruitmentStatus()).isEqualTo(RecruitmentStatus.RECRUITING);
            assertThat(studyMember.getStudyChannel().getId()).isEqualTo(studyChannel.getId());
            assertThat(studyMember.getStudyMemberRole()).isEqualTo(StudyMemberRole.LEADER);

        }

        @DisplayName("스터디 시작날짜는 스터디 종료날짜보다 이전이어야 한다.")
        @Test
        void fail_studyStartDateBefore() {

            // given
            LocalDate startDate = LocalDate.now().plusDays(3);
            StudyChannelCreateRequest request = StudyChannelCreateRequest.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .category(Category.IT)
                    .meetingType(MeetingType.ONLINE)
                    .startDate(startDate)
                    .endDate(startDate.minusDays(1))
                    .build();
            Member member = mock(Member.class);
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> studyChannelService.create(request, 1L))
                    .isExactlyInstanceOf(InvalidStudyDurationException.class)
                    .hasMessage("스터디 시작일은 종료일 이전으로 설정해주세요.");
        }

        @DisplayName("스터디 시작날짜, 스터디 종료날짜는 현재 날짜 이후어야 한다.")
        @Test
        void fail_studyDateAfterToday() {

            // given
            StudyChannelCreateRequest request = StudyChannelCreateRequest.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .category(Category.IT)
                    .meetingType(MeetingType.ONLINE)
                    .startDate(LocalDate.now().minusDays(1L))
                    .endDate(LocalDate.now().minusDays(1L))
                    .build();
            Member member = mock(Member.class);
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> studyChannelService.create(request, 1L))
                    .isExactlyInstanceOf(InvalidStudyStartDateException.class)
                    .hasMessage("스터디 시작 날짜는 오늘 날짜 이후로 설정해주세요.");
        }
    }

    // [ Query ]
    @DisplayName("[스터디 채널 목록 조회 테스트]")
    @Nested
    class getStudyChannelsTest {

        @DisplayName("스터디 채널 목록 조회 - 최신순")
        @Test
        void getStudyChannels() {

            Member member1 = Member.builder().id(1L).name("회원 1").build();
            Member member2 = Member.builder().id(2L).name("회원 2").build();
            Member member3 = Member.builder().id(3L).name("회원 3").build();
            LocalDate now = LocalDate.now();
            StudyChannel studyChannel1 = StudyChannel.builder()
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
                    .region("")
                    .meetingType(MeetingType.ONLINE)
                    .chattingUrl("오픈채팅방 URL")
                    .deposit(10_000)
                    .viewCnt(4)
                    .build();
            StudyChannel studyChannel2 = StudyChannel.builder()
                    .id(2L)
                    .name("스터디명2")
                    .description("스터디 설명2")
                    .studyDuration(StudyDuration.builder()
                            .studyStartDate(now.plusDays(2))
                            .studyEndDate(now.plusMonths(4))
                            .build())
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(5)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING)
                            .build())
                    .category(Category.IT)
                    .region("")
                    .meetingType(MeetingType.ONLINE)
                    .chattingUrl("오픈채팅방 URL")
                    .deposit(10_000)
                    .viewCnt(1)
                    .build();
            StudyChannel studyChannel3 = StudyChannel.builder()
                    .id(3L)
                    .name("스터디명3")
                    .description("스터디 설명3")
                    .studyDuration(StudyDuration.builder()
                            .studyStartDate(now.plusDays(2))
                            .studyEndDate(now.plusMonths(4))
                            .build())
                    .recruitment(Recruitment.builder()
                            .recruitmentNumber(6)
                            .recruitmentStatus(RecruitmentStatus.RECRUITING)
                            .build())
                    .category(Category.EMPLOYMENT)
                    .region("")
                    .meetingType(MeetingType.ONLINE)
                    .chattingUrl("오픈채팅방 URL")
                    .deposit(10_000)
                    .viewCnt(2)
                    .build();

            StudyMember leader1 = StudyMember.leader(member1, studyChannel1);
            StudyMember leader2 = StudyMember.leader(member2, studyChannel2);
            StudyMember leader3 = StudyMember.leader(member3, studyChannel3);
            studyChannel1.getStudyMembers().add(leader1);
            studyChannel2.getStudyMembers().add(leader2);
            studyChannel3.getStudyMembers().add(leader3);

            Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt"));
            SearchCondition searchCondition = new SearchCondition(MeetingType.ONLINE, RecruitmentStatus.RECRUITING, Category.IT);
            PageImpl<StudyChannel> page = new PageImpl<>(List.of(studyChannel1, studyChannel2, studyChannel3), pageable, 3);
            given(studyChannelRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

            List<Long> ids = page.getContent().stream()
                    .map(StudyChannel::getId)
                    .toList();

            given(studyMemberRepository.findAllWithLeader(ids, StudyMemberRole.LEADER))
                    .willReturn(List.of(leader1, leader2, leader3));

            StudyChannelListResponse response = studyChannelService.getStudyChannels(pageable, searchCondition);

            assertThat(response.getTotalPage()).isEqualTo(1);
            assertThat(response.getPageSize()).isEqualTo(6);
            assertThat(response.getPageNumber()).isEqualTo(1);
            assertThat(response.getTotalCount()).isEqualTo(3);
            assertThat(response.getStudyChannels().size()).isEqualTo(3);
            assertThat(response.getStudyChannels().get(0).getStudyChannelId()).isEqualTo(studyChannel1.getId());
            assertThat(response.getStudyChannels().get(0).getMemberId()).isEqualTo(leader1.getMember().getId());
            assertThat(response.getStudyChannels().get(0).getMemberName()).isEqualTo(leader1.getMember().getName());
        }

    }

}