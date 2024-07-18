package com.tenten.studybadge.attendance.service;

import com.tenten.studybadge.attendance.domain.entity.Attendance;
import com.tenten.studybadge.attendance.domain.repository.AttendanceRepository;
import com.tenten.studybadge.attendance.dto.AttendanceCheckRequest;
import com.tenten.studybadge.attendance.dto.AttendanceMember;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.RepeatScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.SingleScheduleRepository;
import com.tenten.studybadge.study.channel.domain.entity.Recruitment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.entity.StudyDuration;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.member.BadgeLevel;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.type.schedule.ScheduleType;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @InjectMocks
    AttendanceService attendanceService;

    @Mock
    AttendanceRepository attendanceRepository;
    @Mock
    StudyMemberRepository studyMemberRepository;
    @Mock
    SingleScheduleRepository singleScheduleRepository;
    @Mock
    RepeatScheduleRepository repeatScheduleRepository;

    @DisplayName("[스터디 채널 출석 체크 테스트]")
    @Nested
    class CheckAttendanceTest {
        List<Member> members;
        StudyChannel studyChannel;
        RepeatSchedule repeatSchedule;
        SingleSchedule singleSchedule;

        @BeforeEach
        void setUp() {
            members = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                Member member = Member.builder()
                        .id((long) i)
                        .name("회원" + i)
                        .banCnt(2)
                        .imgUrl("imageUrl")
                        .badgeLevel(BadgeLevel.SILVER)
                        .build();
                members.add(member);
            }
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
                    .region("")
                    .meetingType(MeetingType.ONLINE)
                    .chattingUrl("오픈채팅방 URL")
                    .deposit(10_000)
                    .viewCnt(4)
                    .build();

            singleSchedule = SingleSchedule.withoutIdBuilder()
                    .scheduleName("Single Meeting")
                    .scheduleContent("Content for single meeting")
                    .scheduleDate(now)
                    .scheduleStartTime(LocalTime.of(10, 0))
                    .scheduleEndTime(LocalTime.of(11, 0))
                    .isRepeated(false)
                    .studyChannel(studyChannel)
                    .placeId(null)
                    .build();

            repeatSchedule = RepeatSchedule.withoutIdBuilder()
                    .scheduleName("Repeat Meeting")
                    .scheduleContent("Content for repeat meeting")
                    .scheduleDate(now)
                    .scheduleStartTime(LocalTime.of(10, 0))
                    .scheduleEndTime(LocalTime.of(11, 0))
                    .repeatCycle(RepeatCycle.WEEKLY)
                    .repeatSituation(RepeatSituation.SUNDAY)
                    .repeatEndDate(LocalDate.of(2024, 12, 29))
                    .isRepeated(true)
                    .studyChannel(studyChannel)
                    .placeId(null)
                    .build();

        }

        @DisplayName("[단일 일정] - 출석 체크를 정상적으로 수행한다.")
        @Test
        void success_checkAttendance() {

            StudyMember leader = StudyMember.builder()
                    .id(1L)
                    .studyChannel(studyChannel)
                    .member(members.get(0))
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .build();

            studyChannel.getStudyMembers().add(leader);

            for (int i = 1; i < 5; i++) {
                StudyMember studyMember = StudyMember.builder()
                        .id((long) (i + 1))
                        .studyChannel(studyChannel)
                        .member(members.get(i))
                        .studyMemberRole(StudyMemberRole.STUDY_MEMBER)
                        .build();
                studyChannel.getStudyMembers().add(studyMember);
            }

            List<AttendanceMember> attendanceMembers = studyChannel.getStudyMembers().stream()
                    .map(studyMember -> AttendanceMember.builder()
                            .isAttendance(true)
                            .studyMemberId(studyMember.getId())
                            .build()
                    ).toList();


            AttendanceCheckRequest attendanceCheckRequest = AttendanceCheckRequest.builder()
                    .attendanceCheckDate(LocalDate.now())
                    .scheduleId(1L)
                    .scheduleType(ScheduleType.SINGLE)
                    .attendanceMembers(attendanceMembers)
                    .build();
            given(studyMemberRepository.findByMemberIdAndStudyChannelId(1L, 1L))
                    .willReturn(Optional.of(leader));
            given(singleScheduleRepository.findById(1L)).willReturn(Optional.of(singleSchedule));
            given(attendanceRepository.findAllBySingleScheduleId(1L)).willReturn(Collections.emptyList());

            attendanceService.checkAttendance(attendanceCheckRequest, 1L, 1L);

            ArgumentCaptor<List<Attendance>> attendanceCaptor = ArgumentCaptor.forClass(List.class);
            verify(attendanceRepository, times(1)).saveAll(attendanceCaptor.capture());
            List<Attendance> attendances = attendanceCaptor.getValue();
            Assertions.assertThat(attendances).hasSize(5);

            for (Attendance attendance: attendances) {
                System.out.print(attendance.getStudyMemberId() + ", " + attendance.getSingleScheduleId() + ", " + attendance.getRepeatScheduleId()
                + ", " + attendance.getAttendanceStatus() + ", " + attendance.getScheduleType() + ", " + attendance.getAttendanceDateTime());
                System.out.println();
            }
        }

        @DisplayName("[반복 일정] - 출석 체크를 정상적으로 수행한다.")
        @Test
        void success_checkAttendance_repeatSchedule() {

            StudyMember leader = StudyMember.builder()
                    .id(1L)
                    .studyChannel(studyChannel)
                    .member(members.get(0))
                    .studyMemberRole(StudyMemberRole.LEADER)
                    .build();

            studyChannel.getStudyMembers().add(leader);

            for (int i = 1; i < 5; i++) {
                StudyMember studyMember = StudyMember.builder()
                        .id((long) (i + 1))
                        .studyChannel(studyChannel)
                        .member(members.get(i))
                        .studyMemberRole(StudyMemberRole.STUDY_MEMBER)
                        .build();
                studyChannel.getStudyMembers().add(studyMember);
            }

            List<AttendanceMember> attendanceMembers = studyChannel.getStudyMembers().stream()
                    .map(studyMember -> AttendanceMember.builder()
                            .isAttendance(true)
                            .studyMemberId(studyMember.getId())
                            .build()
                    ).toList();


            AttendanceCheckRequest attendanceCheckRequest = AttendanceCheckRequest.builder()
                    .attendanceCheckDate(LocalDate.now())
                    .scheduleId(1L)
                    .scheduleType(ScheduleType.REPEAT)
                    .attendanceMembers(attendanceMembers)
                    .build();
            LocalDate now = LocalDate.now();
            LocalDateTime startDateTime = now.atStartOfDay();
            LocalDateTime endDateTime = startDateTime.plusDays(1);
            given(studyMemberRepository.findByMemberIdAndStudyChannelId(1L, 1L))
                    .willReturn(Optional.of(leader));
            given(repeatScheduleRepository.findById(1L)).willReturn(Optional.of(repeatSchedule));
            given(attendanceRepository.findAllByRepeatScheduleIdAndAttendanceDateTimeBetween(1L, startDateTime, endDateTime)).willReturn(Collections.emptyList());

            attendanceService.checkAttendance(attendanceCheckRequest, 1L, 1L);

            ArgumentCaptor<List<Attendance>> attendanceCaptor = ArgumentCaptor.forClass(List.class);
            verify(attendanceRepository, times(1)).saveAll(attendanceCaptor.capture());
            List<Attendance> attendances = attendanceCaptor.getValue();
            Assertions.assertThat(attendances).hasSize(5);

            for (Attendance attendance: attendances) {
                System.out.print(attendance.getStudyMemberId() + ", " + attendance.getSingleScheduleId() + ", " + attendance.getRepeatScheduleId()
                        + ", " + attendance.getAttendanceStatus() + ", " + attendance.getScheduleType() + ", " + attendance.getAttendanceDateTime());
                System.out.println();
            }
        }
    }

}