package com.tenten.studybadge.study.channel.service;

import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.study.channel.domain.entity.Recruitment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.entity.StudyDuration;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.channel.dto.SearchCondition;
import com.tenten.studybadge.study.channel.dto.StudyChannelDetailsResponse;
import com.tenten.studybadge.study.channel.dto.StudyChannelListResponse;
import com.tenten.studybadge.study.deposit.domain.repository.StudyChannelDepositRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class StudyChannelQueryServiceTest {

    @InjectMocks
    private StudyChannelQueryService studyChannelQueryService;

    @Mock
    private StudyChannelRepository studyChannelRepository;
    @Mock
    private StudyMemberRepository studyMemberRepository;
    @Mock
    private StudyChannelDepositRepository studyChannelDepositRepository;
    @Mock
    private MemberRepository memberRepository;


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

            StudyChannelListResponse response = studyChannelQueryService.getStudyChannels(pageable, searchCondition);

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

    @DisplayName("[특정 스터디 채널 조회 테스트]")
    @Nested
    class getStudyChannelTest {

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

        @DisplayName("특정 스터디 채널 조회 - 스터디 멤버가 조회했을 경우")
        @Test
        void getStudyChannel_studyMember() {

            StudyMember leader = StudyMember.leader(member1, studyChannel);
            StudyMember studyMember1 = StudyMember.member(member2, studyChannel);
            StudyMember studyMember2 = StudyMember.member(member3, studyChannel);

            studyChannel.getStudyMembers().add(leader);
            studyChannel.getStudyMembers().add(studyMember1);
            studyChannel.getStudyMembers().add(studyMember2);

            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));
            given(studyChannelRepository.existsById(1L)).willReturn(true);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member1));

            StudyChannelDetailsResponse response = studyChannelQueryService.getStudyChannel(1L, 1L);

            assertThat(response.getStudyChannelId()).isEqualTo(1L);
            assertThat(response.getChattingUrl()).isNotNull();
            assertThat(response.getChattingUrl()).isEqualTo("오픈채팅방 URL");
            assertThat(response.getStudyChannelName()).isEqualTo("스터디명");
            assertThat(response.getStudyChannelDescription()).isEqualTo("스터디 설명");
            assertThat(response.getCapacity()).isEqualTo(6);
            assertThat(response.getCategory()).isEqualTo(Category.IT);
            assertThat(response.getMeetingType()).isEqualTo(MeetingType.ONLINE);
            assertThat(response.getRegion()).isNull();
            assertThat(response.getDeposit()).isEqualTo(10_000);
            assertThat(response.isLeader()).isTrue();
            assertThat(response.getLeaderName()).isEqualTo("회원 1");
            assertThat(response.getSubLeaderName()).isEqualTo("회원 1");

        }

        @DisplayName("특정 스터디 채널 조회 - 스터디 멤버가 아닌 회원이 조회 했을 경우 채팅 URL을 볼 수 없다.")
        @Test
        void getStudyChannel_notStudyMember() {

            StudyMember leader = StudyMember.leader(member1, studyChannel);
            StudyMember studyMember = StudyMember.member(member2, studyChannel);

            studyChannel.getStudyMembers().add(leader);
            studyChannel.getStudyMembers().add(studyMember);

            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));
            given(studyChannelRepository.existsById(1L)).willReturn(true);
            given(memberRepository.findById(3L)).willReturn(Optional.of(member3));

            StudyChannelDetailsResponse response = studyChannelQueryService.getStudyChannel(1L, 3L);

            assertThat(response.getStudyChannelId()).isEqualTo(1L);
            assertThat(response.getChattingUrl()).isNull();
            assertThat(response.getStudyChannelName()).isEqualTo("스터디명");
            assertThat(response.getStudyChannelDescription()).isEqualTo("스터디 설명");
            assertThat(response.getCapacity()).isEqualTo(6);
            assertThat(response.getCategory()).isEqualTo(Category.IT);
            assertThat(response.getMeetingType()).isEqualTo(MeetingType.ONLINE);
            assertThat(response.getRegion()).isNull();
            assertThat(response.getDeposit()).isEqualTo(10_000);
            assertThat(response.isLeader()).isFalse();
            assertThat(response.getLeaderName()).isEqualTo("회원 1");
            assertThat(response.getSubLeaderName()).isEqualTo("회원 1");

        }

        @DisplayName("특정 스터디 채널 조회 - 부 리더가 있을 경우")
        @Test
        void getStudyChannel_withSubLeader() {

            StudyMember leader = StudyMember.leader(member1, studyChannel);
            StudyMember subLeader = StudyMember.builder()
                    .member(member2)
                    .studyChannel(studyChannel)
                    .studyMemberRole(StudyMemberRole.SUB_LEADER)
                    .build();
            StudyMember studyMember = StudyMember.member(member3, studyChannel);

            studyChannel.getStudyMembers().add(leader);
            studyChannel.getStudyMembers().add(subLeader);
            studyChannel.getStudyMembers().add(studyMember);

            given(studyChannelRepository.findByIdWithMember(1L)).willReturn(Optional.of(studyChannel));
            given(studyChannelRepository.existsById(1L)).willReturn(true);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member1));

            StudyChannelDetailsResponse response = studyChannelQueryService.getStudyChannel(1L, 1L);

            assertThat(response.getStudyChannelId()).isEqualTo(1L);
            assertThat(response.getChattingUrl()).isNotNull();
            assertThat(response.getChattingUrl()).isEqualTo("오픈채팅방 URL");
            assertThat(response.getStudyChannelName()).isEqualTo("스터디명");
            assertThat(response.getStudyChannelDescription()).isEqualTo("스터디 설명");
            assertThat(response.getCapacity()).isEqualTo(6);
            assertThat(response.getCategory()).isEqualTo(Category.IT);
            assertThat(response.getMeetingType()).isEqualTo(MeetingType.ONLINE);
            assertThat(response.getRegion()).isNull();
            assertThat(response.getDeposit()).isEqualTo(10_000);
            assertThat(response.isLeader()).isTrue();
            assertThat(response.getLeaderName()).isEqualTo("회원 1");
            assertThat(response.getSubLeaderName()).isEqualTo("회원 2");

        }

    }


}
