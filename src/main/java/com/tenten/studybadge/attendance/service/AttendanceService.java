package com.tenten.studybadge.attendance.service;

import com.tenten.studybadge.attendance.domain.entity.Attendance;
import com.tenten.studybadge.attendance.domain.repository.AttendanceRepository;
import com.tenten.studybadge.attendance.dto.AttendanceCheckRequest;
import com.tenten.studybadge.attendance.dto.AttendanceInfoResponse;
import com.tenten.studybadge.attendance.dto.AttendanceMember;
import com.tenten.studybadge.common.exception.attendance.InvalidAttendanceCheckDateException;
import com.tenten.studybadge.common.exception.schedule.NotFoundRepeatScheduleException;
import com.tenten.studybadge.common.exception.schedule.NotFoundSingleScheduleException;
import com.tenten.studybadge.common.exception.schedule.NotIncludedInRepeatScheduleException;
import com.tenten.studybadge.common.exception.studychannel.NotStudyLeaderException;
import com.tenten.studybadge.common.exception.studychannel.NotStudyMemberException;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.RepeatScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.SingleScheduleRepository;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.attendance.AttendanceStatus;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.type.schedule.ScheduleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public List<AttendanceInfoResponse> getAttendanceRatioForStudyChannel(Long studyChannelId, Long memberId) {

        studyMemberRepository.findByMemberIdAndStudyChannelId(memberId, studyChannelId).orElseThrow(NotStudyMemberException::new);
        List<StudyMember> studyMembers = studyMemberRepository.findAllActiveStudyMembers(studyChannelId);
        List<SingleSchedule> singleSchedules = singleScheduleRepository.findAllByStudyChannelId(studyChannelId);
        List<RepeatSchedule> repeatSchedules = repeatScheduleRepository.findAllByStudyChannelId(studyChannelId);

        int totalDays = countAllScheduleDays(singleSchedules, repeatSchedules);

        // 스터디 멤버 별 총 출석 일수
        Map<Long, Long> studyMemberAttendanceCountMap = studyMembers.stream().collect(Collectors.toMap(StudyMember::getId, (studyMember) -> 0L));

        // 반복 일정
        for (RepeatSchedule repeatSchedule : repeatSchedules) {
            List<Attendance> repeatScheduleAttendances = attendanceRepository.findAllByRepeatScheduleId(repeatSchedule.getId());
            Map<Long, List<Attendance>> listMap = groupByStudyMember(repeatScheduleAttendances);

            for (Map.Entry<Long, List<Attendance>> entry : listMap.entrySet()) {
                Long studyMemberId = entry.getKey();
                List<Attendance> attendances = entry.getValue();

                long attendanceDays = attendances.stream().filter(attendance -> attendance.getAttendanceStatus().equals(AttendanceStatus.ATTENDANCE)).count();
                studyMemberAttendanceCountMap.put(studyMemberId, studyMemberAttendanceCountMap.getOrDefault(studyMemberId, 0L) + attendanceDays);
            }
        }

        // 단일 일정
        for (SingleSchedule singleSchedule : singleSchedules) {
            List<Attendance> singleScheduleAttendances = attendanceRepository.findAllBySingleScheduleId(singleSchedule.getId());
            singleScheduleAttendances.stream()
                    .filter(attendance -> attendance.getAttendanceStatus().equals(AttendanceStatus.ATTENDANCE))
                    .forEach(attendance -> studyMemberAttendanceCountMap.put(
                            attendance.getStudyMemberId(),
                            studyMemberAttendanceCountMap.getOrDefault(attendance.getStudyMemberId(), 0L) + 1));
        }

        List<AttendanceInfoResponse> attendanceInfoResponses = new ArrayList<>();

        for (StudyMember studyMember : studyMembers) {
            long attendanceDays = studyMemberAttendanceCountMap.get(studyMember.getId());
            double attendanceRatio = totalDays == 0 ? 0.0 : (double) attendanceDays * 100 / totalDays;
            attendanceInfoResponses.add(AttendanceInfoResponse.builder()
                    .memberId(studyMember.getMember().getId())
                    .studyMemberId(studyMember.getId())
                    .name(studyMember.getMember().getName())
                    .imageUrl(studyMember.getMember().getImgUrl())
                    .attendanceCount(attendanceDays)
                    .attendanceRatio(attendanceRatio)
                    .build());
        }
        return attendanceInfoResponses;
    }

    public double getAttendanceRatioForMember(StudyMember studyMember) {
        StudyChannel studyChannel = studyMember.getStudyChannel();
        List<SingleSchedule> singleSchedules = singleScheduleRepository.findAllByStudyChannelId(studyChannel.getId());
        List<RepeatSchedule> repeatSchedules = repeatScheduleRepository.findAllByStudyChannelId(studyChannel.getId());

        int totalDays = countAllScheduleDays(singleSchedules, repeatSchedules);
        long totalAttendanceDays = 0;

        // 반복 일정
        for (RepeatSchedule repeatSchedule : repeatSchedules) {
            List<Attendance> repeatScheduleAttendances = attendanceRepository.findAllByRepeatScheduleIdAndStudyMemberId(repeatSchedule.getId(), studyMember.getId());
            totalAttendanceDays += repeatScheduleAttendances.stream()
                    .filter(Attendance::isAttendance)
                    .count();
        }

        // 단일 일정
        for (SingleSchedule singleSchedule : singleSchedules) {
            List<Attendance> singleScheduleAttendances = attendanceRepository.findAllBySingleScheduleIdAndStudyMemberId(singleSchedule.getId(), studyMember.getId());
            totalAttendanceDays += singleScheduleAttendances.stream()
                    .filter(Attendance::isAttendance)
                    .count();
        }

        return (double) (totalAttendanceDays * 100) / totalDays;
    }

    private Map<Long, List<Attendance>> groupByStudyMember(List<Attendance> repeatScheduleAttendances) {
        return repeatScheduleAttendances.stream().collect(Collectors.groupingBy(Attendance::getStudyMemberId));
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
            throw new NotIncludedInRepeatScheduleException();
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

    private int calculate(LocalDate startDate, LocalDate endDate, RepeatCycle repeatCycle, RepeatSituation repeatSituation) {
        LocalDate currentDate = startDate;
        int count = 0;
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            count++;
            switch (repeatCycle) {
                case DAILY: {
                    currentDate = currentDate.plusDays(1);
                    break;
                }
                case WEEKLY: {
                    currentDate = currentDate.plusWeeks(1);
                    break;
                }
                case MONTHLY: {
                    currentDate = currentDate.plusMonths((Integer) repeatSituation.getDescription());
                    break;
                }
            }
        }
        return count;
    }

    private int countAllScheduleDays(List<SingleSchedule> singleSchedules, List<RepeatSchedule> repeatSchedules) {
        // 1) 단일 일정 수
        int count0 = singleSchedules.size();

        // 2) 반복 일정 수
        int count1 = 0;
        for (RepeatSchedule repeatSchedule : repeatSchedules) {
            count1 += calculate(repeatSchedule.getScheduleDate(), repeatSchedule.getRepeatEndDate(), repeatSchedule.getRepeatCycle(), repeatSchedule.getRepeatSituation());
        }
        return count0 + count1;
    }

    private void checkLeader(Long memberId, Long studyChannelId) {
        StudyMember studyMember = studyMemberRepository.findByMemberIdAndStudyChannelId(memberId, studyChannelId).orElseThrow(NotStudyMemberException::new);
        if (!studyMember.isLeader()) {
            throw new NotStudyLeaderException();
        }
    }
}
