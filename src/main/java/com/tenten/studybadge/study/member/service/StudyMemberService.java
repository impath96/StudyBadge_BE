package com.tenten.studybadge.study.member.service;

import com.tenten.studybadge.attendance.domain.entity.Attendance;
import com.tenten.studybadge.attendance.domain.repository.AttendanceRepository;
import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.schedule.NotFoundRepeatScheduleException;
import com.tenten.studybadge.common.exception.schedule.NotFoundSingleScheduleException;
import com.tenten.studybadge.common.exception.studychannel.*;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.RepeatScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.SingleScheduleRepository;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.study.member.dto.ScheduleStudyMemberResponse;
import com.tenten.studybadge.study.member.dto.StudyMemberInfoResponse;
import com.tenten.studybadge.study.member.dto.StudyMembersResponse;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;
    private final StudyChannelRepository studyChannelRepository;
    private final MemberRepository memberRepository;
    private final SingleScheduleRepository singleScheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final RepeatScheduleRepository repeatScheduleRepository;

    public StudyMembersResponse getStudyMembers(Long studyChannelId, Long memberId) {

        if (!studyMemberRepository.existsByStudyChannelIdAndMemberId(studyChannelId, memberId)) {
            throw new NotStudyMemberException();
        }
        List<StudyMember> studyMembers = studyMemberRepository.findAllByStudyChannelIdWithMember(studyChannelId);

        boolean isLeader = studyMembers.stream()
                .anyMatch(studyMember -> studyMember.getMember().getId().equals(memberId) && studyMember.isLeader());

        List<StudyMemberInfoResponse> responses = studyMembers.stream()
                .map(StudyMember::toResponse)
                .toList();

        return StudyMembersResponse.builder()
                .studyMembers(responses)
                .isLeader(isLeader)
                .build();
    }

    public void assignStudyLeaderRole(Long studyChannelId, Long memberId, Long studyMemberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundMemberException::new);
        StudyChannel studyChannel = studyChannelRepository.findByIdWithMember(studyChannelId).orElseThrow(NotFoundStudyChannelException::new);

        checkLeader(studyChannel, member);

        StudyMember subLeader = studyChannel.getSubLeader();
        StudyMember target = getStudyMember(studyChannel, studyMemberId);

        if (subLeader != null) {
            throw new AlreadyExistsSubLeaderException();
        }

        target.setStudyMemberRole(StudyMemberRole.SUB_LEADER);
        studyMemberRepository.save(target);

    }

    public List<ScheduleStudyMemberResponse> getStudyMembersSingleSchedule(Long studyChannelId, Long scheduleId, Long memberId) {
        studyMemberRepository.findByMemberIdAndStudyChannelId(studyChannelId, memberId).orElseThrow(NotStudyMemberException::new);
        SingleSchedule singleSchedule = singleScheduleRepository.findById(scheduleId).orElseThrow(NotFoundSingleScheduleException::new);
        LocalDate scheduleDate = singleSchedule.getScheduleDate();
        LocalDate currentDate = LocalDate.now();

        List<StudyMember> studyMembers = studyMemberRepository.findAllByStudyChannelIdWithMember(studyChannelId);

        if (scheduleDate.isBefore(currentDate)) {
            List<Attendance> attendances = attendanceRepository.findAllBySingleScheduleId(scheduleId);
            return response(studyMembers, attendances);
        }
        return response(studyMembers, Collections.emptyList());
    }

    public List<ScheduleStudyMemberResponse> getStudyMembersRepeatSchedule(Long studyChannelId, Long scheduleId, Long memberId, LocalDate date) {
        studyMemberRepository.findByMemberIdAndStudyChannelId(studyChannelId, memberId).orElseThrow(NotStudyMemberException::new);
        RepeatSchedule repeatSchedule = repeatScheduleRepository.findById(scheduleId).orElseThrow(NotFoundRepeatScheduleException::new);
        validate(repeatSchedule, date);
        LocalDate currentDate = LocalDate.now();

        List<StudyMember> studyMembers = studyMemberRepository.findAllByStudyChannelIdWithMember(studyChannelId);

        if (date.isBefore(currentDate)) {
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = startDateTime.plusDays(1);
            List<Attendance> attendances = attendanceRepository.findAllByRepeatScheduleIdAndAttendanceDateTimeBetween(scheduleId, startDateTime, endDateTime);
            return response(studyMembers, attendances);
        }
        return response(studyMembers, Collections.emptyList());
    }

    public void leaveStudyChannel(Long studyChannelId, Long studyMemberId, Long memberId) {
        StudyMember studyMember = studyMemberRepository.findById(studyMemberId).orElseThrow(NotStudyMemberException::new);
        if (!studyMember.getStudyChannel().getId().equals(studyChannelId)) {
            throw new NotStudyMemberException();
        }
        if (!studyMember.getMember().getId().equals(memberId)) {
            throw new NotStudyMemberException();
        }
        if (studyMember.isLeader()) {
            throw new LeaveStudyLeaderException();
        }
        studyMemberRepository.delete(studyMember);
    }

    private void validate(RepeatSchedule repeatSchedule, LocalDate date) {
        LocalDate startDate = repeatSchedule.getScheduleDate();
        LocalDate endDate = repeatSchedule.getRepeatEndDate();
        RepeatCycle repeatCycle = repeatSchedule.getRepeatCycle();
        RepeatSituation repeatSituation = repeatSchedule.getRepeatSituation();
        validateInRange(date, startDate, endDate);
        LocalDate currentDate = startDate;
        boolean isMatched = false;
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            if (currentDate.isEqual(date)) {
                isMatched = true;
                break;
            }
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
        if (!isMatched) {
            throw new NotFoundRepeatScheduleException();
        }
    }

    private List<ScheduleStudyMemberResponse> response(List<StudyMember> studyMembers, List<Attendance> attendances) {
        List<ScheduleStudyMemberResponse> responses = studyMembers.stream()
                .map((studyMember -> ScheduleStudyMemberResponse.builder()
                        .memberId(studyMember.getMember().getId())
                        .studyMemberId(studyMember.getId())
                        .name(studyMember.getMember().getName())
                        .imageUrl(studyMember.getMember().getImgUrl())
                        .build()
                ))
                .toList();
        if (attendances.isEmpty()) {
            return responses;
        }
        Map<Long, Attendance> attendanceMap = attendances.stream().collect(Collectors.toMap(Attendance::getStudyMemberId, Function.identity()));
        responses.forEach((response) -> {
            response.setAttendanceStatus(attendanceMap.get(response.getStudyMemberId()).getAttendanceStatus());
            response.setAttendance(attendanceMap.get(response.getStudyMemberId()).isAttendance());
        });
        return responses;
    }

    private StudyMember getStudyMember(StudyChannel studyChannel, Long studyMemberId) {
        return studyChannel.getStudyMembers().stream()
                .filter(studyMember -> studyMember.getId().equals(studyMemberId))
                .findFirst()
                .orElseThrow(NotStudyMemberException::new);
    }

    private void checkLeader(StudyChannel studyChannel, Member member) {
        if (!studyChannel.isLeader(member)) {
            throw new NotStudyLeaderException();
        }
    }

    private void validateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            throw new NotFoundRepeatScheduleException();
        }
    }

}
