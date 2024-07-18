package com.tenten.studybadge.attendance.service;

import com.tenten.studybadge.attendance.domain.entity.Attendance;
import com.tenten.studybadge.attendance.domain.repository.AttendanceRepository;
import com.tenten.studybadge.attendance.dto.AttendanceCheckRequest;
import com.tenten.studybadge.attendance.dto.AttendanceMember;
import com.tenten.studybadge.common.exception.attendance.InvalidAttendanceCheckDateException;
import com.tenten.studybadge.common.exception.schedule.NotFoundRepeatScheduleException;
import com.tenten.studybadge.common.exception.schedule.NotFoundSingleScheduleException;
import com.tenten.studybadge.common.exception.schedule.OutRangeScheduleException;
import com.tenten.studybadge.common.exception.studychannel.NotStudyLeaderException;
import com.tenten.studybadge.common.exception.studychannel.NotStudyMemberException;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.RepeatScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.SingleScheduleRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.attendance.AttendanceStatus;
import com.tenten.studybadge.type.schedule.ScheduleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final SingleScheduleRepository singleScheduleRepository;
    private final RepeatScheduleRepository repeatScheduleRepository;

    public void checkAttendance(AttendanceCheckRequest attendanceCheckRequest, Long memberId, Long studyChannelId) {
        checkLeader(memberId, studyChannelId);
        ScheduleType scheduleType = attendanceCheckRequest.getScheduleType();

        if (scheduleType.equals(ScheduleType.SINGLE)) {
            checkAttendanceForSingleSchedule(attendanceCheckRequest);
        } else {
            checkAttendanceForRepeatSchedule(attendanceCheckRequest);
        }
    }

    private void checkAttendanceForSingleSchedule(AttendanceCheckRequest attendanceCheckRequest) {
        SingleSchedule singleSchedule = singleScheduleRepository.findById(attendanceCheckRequest.getScheduleId()).orElseThrow(NotFoundSingleScheduleException::new);
        LocalDate attendanceCheckDate = attendanceCheckRequest.getAttendanceCheckDate();
        LocalDateTime currentTime = LocalDateTime.now();

        if (!singleSchedule.getScheduleDate().equals(attendanceCheckDate) || !currentTime.toLocalDate().isEqual(attendanceCheckDate)) {
            throw new InvalidAttendanceCheckDateException();
        }

        List<Attendance> attendanceList = attendanceRepository.findAllBySingleScheduleId(attendanceCheckRequest.getScheduleId());

        if (attendanceList.isEmpty()) {
            saveAttendances(attendanceCheckRequest, ScheduleType.SINGLE, singleSchedule.getId());
        } else {
            updateAttendances(attendanceCheckRequest, attendanceList);
        }
    }

    private void checkAttendanceForRepeatSchedule(AttendanceCheckRequest attendanceCheckRequest) {
        RepeatSchedule repeatSchedule = repeatScheduleRepository.findById(attendanceCheckRequest.getScheduleId()).orElseThrow(NotFoundRepeatScheduleException::new);
        LocalDate attendanceCheckDate = attendanceCheckRequest.getAttendanceCheckDate();
        LocalDateTime currentTime = LocalDateTime.now();

        if (repeatSchedule.getScheduleDate().isAfter(attendanceCheckDate) || repeatSchedule.getRepeatEndDate().isBefore(attendanceCheckDate)) {
            throw new OutRangeScheduleException();
        }

        if (!currentTime.toLocalDate().isEqual(attendanceCheckDate)) {
            throw new InvalidAttendanceCheckDateException();
        }

        LocalDateTime startDateTime = attendanceCheckDate.atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusDays(1);
        List<Attendance> attendanceList = attendanceRepository.findAllByRepeatScheduleIdAndAttendanceDateTimeBetween(attendanceCheckRequest.getScheduleId(), startDateTime, endDateTime);

        if (attendanceList.isEmpty()) {
            saveAttendances(attendanceCheckRequest, ScheduleType.REPEAT, repeatSchedule.getId());
        } else {
            updateAttendances(attendanceCheckRequest, attendanceList);
        }
    }

    private void updateAttendances(AttendanceCheckRequest attendanceCheckRequest, List<Attendance> attendanceList) {

        Map<Long, AttendanceMember> attendanceMemberMap = attendanceCheckRequest.getAttendanceMembers().stream().collect(
                Collectors.toMap(AttendanceMember::getStudyMemberId, Function.identity())
        );

        for (Attendance attendance : attendanceList) {
            Long studyMemberId = attendance.getStudyMemberId();
            AttendanceMember attendanceMember;
            if (!attendanceMemberMap.containsKey(studyMemberId)) {
                continue;
            }
            attendanceMember = attendanceMemberMap.get(studyMemberId);
            attendance.setAttendanceStatus(attendanceMember.getIsAttendance() ? AttendanceStatus.ATTENDANCE : AttendanceStatus.ABSENCE);
        }
        attendanceRepository.saveAll(attendanceList);
    }

    private void saveAttendances(AttendanceCheckRequest attendanceCheckRequest, ScheduleType scheduleType, Long scheduleId) {

        List<Attendance> attendances = attendanceCheckRequest.getAttendanceMembers().stream()
                .map(attendanceMember -> Attendance.builder()
                        .attendanceDateTime(LocalDateTime.now())
                        .attendanceStatus(attendanceMember.getIsAttendance() ? AttendanceStatus.ATTENDANCE : AttendanceStatus.ABSENCE)
                        .scheduleType(scheduleType)
                        .studyMemberId(attendanceMember.getStudyMemberId())
                        .build()
                ).toList();

        if (scheduleType.equals(ScheduleType.SINGLE)) {
            attendances.forEach(attendance -> attendance.setSingleScheduleId(scheduleId));
        } else {
            attendances.forEach(attendance -> attendance.setRepeatScheduleId(scheduleId));
        }
        attendanceRepository.saveAll(attendances);
    }


    private void checkLeader(Long memberId, Long studyChannelId) {
        StudyMember studyMember = studyMemberRepository.findByMemberIdAndStudyChannelId(memberId, studyChannelId).orElseThrow(NotStudyMemberException::new);
        if (!studyMember.isLeader()) {
            throw new NotStudyLeaderException();
        }
    }
}
