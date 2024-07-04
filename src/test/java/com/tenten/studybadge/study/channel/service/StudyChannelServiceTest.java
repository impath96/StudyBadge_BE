package com.tenten.studybadge.study.channel.service;

import com.tenten.studybadge.common.exception.studychannel.InvalidStudyDurationException;
import com.tenten.studybadge.common.exception.studychannel.InvalidStudyStartDateException;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.channel.dto.StudyChannelCreateRequest;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class StudyChannelServiceTest {

    @Autowired
    private StudyChannelService studyChannelService;

    @Autowired
    private StudyChannelRepository studyChannelRepository;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @BeforeEach
    void beforeEach() {
        studyMemberRepository.deleteAll();
        studyChannelRepository.deleteAll();
    }

    @DisplayName("[스터디 채널 생성 테스트]")
    @Nested
    class CreateStudyChannelTest {

        @DisplayName("스터디 채널을 생성한 회원은 채널의 스터디 멤버가 되고 역할은 리더가 된다.")
        @Test
        void success_createStudyChannel() {

            StudyChannelCreateRequest request = StudyChannelCreateRequest.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .startDate(LocalDate.of(2024, 7, 10))
                    .endDate(LocalDate.of(2024, 7, 20))
                    .recruitmentNumber(8)
                    .minRecruitmentNumber(4)
                    .category("IT")
                    .region("")
                    .meetingType("ONLINE")
                    .chattingUrl("오픈채팅방 URL")
                    .deposit(10_000)
                    .depositDescription("스터디 채널 승인 시 자동으로 빠져나갑니다.")
                    .build();

            studyChannelService.create(request, 1L);

            StudyChannel studyChannel = studyChannelRepository.findAll().stream().findFirst().get();
            StudyMember studyMember = studyMemberRepository.findAll().stream().findFirst().get();

            assertThat(studyChannel.getRecruitment().getRecruitmentStatus()).isEqualTo(RecruitmentStatus.RECRUITING);
            assertThat(studyMember.getStudyChannel().getId()).isEqualTo(studyChannel.getId());
            assertThat(studyMember.getStudyMemberRole()).isEqualTo(StudyMemberRole.LEADER);

        }

        @DisplayName("스터디 시작날짜는 스터디 종료날짜보다 이전이어야 한다.")
        @Test
        void fail_studyStartDateBefore() {

            LocalDate startDate = LocalDate.now().plusDays(3);
            StudyChannelCreateRequest request = StudyChannelCreateRequest.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .category("IT")
                    .meetingType("ONLINE")
                    .startDate(startDate)
                    .endDate(startDate.minusDays(1))
                    .build();

            assertThatThrownBy(() -> studyChannelService.create(request, 1L))
                    .isExactlyInstanceOf(InvalidStudyDurationException.class)
                    .hasMessage("스터디 시작일은 종료일 이전으로 설정해주세요.");
        }

        @DisplayName("스터디 시작날짜, 스터디 종료날짜는 현재 날짜 이후어야 한다.")
        @Test
        void fail_studyDateAfterToday() {

            StudyChannelCreateRequest request = StudyChannelCreateRequest.builder()
                    .name("스터디명")
                    .description("스터디 설명")
                    .category("IT")
                    .meetingType("ONLINE")
                    .startDate(LocalDate.now().minusDays(1L))
                    .endDate(LocalDate.now().minusDays(1L))
                    .build();

            assertThatThrownBy(() -> studyChannelService.create(request, 1L))
                    .isExactlyInstanceOf(InvalidStudyStartDateException.class)
                    .hasMessage("스터디 시작 날짜는 오늘 날짜 이후로 설정해주세요.");
        }
    }

}