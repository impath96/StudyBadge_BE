package com.tenten.studybadge.common.batch;

import com.tenten.studybadge.attendance.domain.entity.Attendance;
import com.tenten.studybadge.attendance.domain.repository.AttendanceRepository;
import com.tenten.studybadge.attendance.dto.AttendanceInfoResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AttendanceRatioProcessor implements ItemProcessor<StudyChannel, StudyMemberAttendanceRatioList> {

    private final AttendanceRepository attendanceRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final SingleScheduleRepository singleScheduleRepository;
    private final RepeatScheduleRepository repeatScheduleRepository;

    @Override
    public StudyMemberAttendanceRatioList process(StudyChannel studyChannel) throws Exception {
        List<AttendanceInfoResponse> responses = getAttendanceRatioForStudyChannel(studyChannel);
        return StudyMemberAttendanceRatioList.builder()
                .studyMemberAttendanceRatioList(responses.stream()
                .map((response) -> new StudyMemberAttendanceRatio(response.getStudyMemberId(), response.getAttendanceRatio()))
                .toList())
                .build();
    }

    public List<AttendanceInfoResponse> getAttendanceRatioForStudyChannel(StudyChannel studyChannel) {

        List<StudyMember> studyMembers = studyMemberRepository.findAllByStudyChannelIdWithMember(studyChannel.getId());
        List<SingleSchedule> singleSchedules = singleScheduleRepository.findAllByStudyChannelId(studyChannel.getId());
        List<RepeatSchedule> repeatSchedules = repeatScheduleRepository.findAllByStudyChannelId(studyChannel.getId());

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
            double attendanceRatio = (double) attendanceDays * 100 / totalDays;
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

    private Map<Long, List<Attendance>> groupByStudyMember(List<Attendance> repeatScheduleAttendances) {
        return repeatScheduleAttendances.stream().collect(Collectors.groupingBy(Attendance::getStudyMemberId));
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


}
