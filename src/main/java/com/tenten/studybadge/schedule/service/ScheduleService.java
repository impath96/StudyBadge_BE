package com.tenten.studybadge.schedule.service;

import static com.tenten.studybadge.common.constant.NotificationConstant.REPEAT_SCHEDULE_CREATE;
import static com.tenten.studybadge.common.constant.NotificationConstant.REPEAT_SCHEDULE_DELETE;
import static com.tenten.studybadge.common.constant.NotificationConstant.REPEAT_SCHEDULE_URL;
import static com.tenten.studybadge.common.constant.NotificationConstant.SINGLE_SCHEDULE_CREATE;
import static com.tenten.studybadge.common.constant.NotificationConstant.SINGLE_SCHEDULE_DELETE;
import static com.tenten.studybadge.common.constant.NotificationConstant.SINGLE_SCHEDULE_URL;

import com.tenten.studybadge.common.constant.NotificationConstant;
import com.tenten.studybadge.common.exception.schedule.CanNotDeleteForBeforeDateException;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForRepeatScheduleEditRequestException;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForRepeatSituationException;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleEditRequestException;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleRequestException;
import com.tenten.studybadge.common.exception.schedule.InvalidScheduleModificationException;
import com.tenten.studybadge.common.exception.schedule.NotEqualSingleScheduleDate;
import com.tenten.studybadge.common.exception.schedule.NotFoundRepeatScheduleException;
import com.tenten.studybadge.common.exception.schedule.NotFoundSingleScheduleException;
import com.tenten.studybadge.common.exception.schedule.OutRangeScheduleException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.common.exception.studychannel.NotStudyLeaderException;
import com.tenten.studybadge.common.exception.studychannel.NotStudyMemberException;
import com.tenten.studybadge.notification.service.NotificationService;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.RepeatScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.SingleScheduleRepository;
import com.tenten.studybadge.schedule.dto.RepeatScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.RepeatScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleDeleteRequest;
import com.tenten.studybadge.schedule.dto.ScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleResponse;
import com.tenten.studybadge.schedule.dto.SingleScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.SingleScheduleEditRequest;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.notification.NotificationType;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.type.schedule.ScheduleType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final SingleScheduleRepository singleScheduleRepository;
    private final RepeatScheduleRepository repeatScheduleRepository;
    private final StudyChannelRepository studyChannelRepository;
    private final StudyMemberRepository studyMemberRepository;

    private final NotificationService notificationService;

    public void postSingleSchedule(SingleScheduleCreateRequest singleScheduleCreateRequest, Long studyChannelId) {
        StudyChannel studyChannel = validateStudyChannel(studyChannelId);

        validateStudyLeader(singleScheduleCreateRequest.getMemberId(), studyChannelId);

        SingleSchedule saveSingleSchedule = singleScheduleRepository.save(createSingleScheduleFromRequest(
            singleScheduleCreateRequest, studyChannel));

        sendNotificationForScheduleCreate(studyChannelId, saveSingleSchedule.getId(),
            saveSingleSchedule.getScheduleDate(), NotificationType.SCHEDULE_CREATE,
            SINGLE_SCHEDULE_URL, SINGLE_SCHEDULE_CREATE);
    }

    public void postRepeatSchedule(RepeatScheduleCreateRequest repeatScheduleCreateRequest, Long studyChannelId) {
        StudyChannel studyChannel = validateStudyChannel(studyChannelId);

        RepeatCycle repeatCycle = repeatScheduleCreateRequest.getRepeatCycle();
        LocalDate scheduleDate = repeatScheduleCreateRequest.getScheduleDate();
        RepeatSituation repeatSituation = repeatScheduleCreateRequest.getRepeatSituation();
        validateRepeatSituation(scheduleDate, repeatCycle, repeatSituation);

        validateStudyLeader(repeatScheduleCreateRequest.getMemberId(), studyChannelId);

        RepeatSchedule saveRepeatSchedule = repeatScheduleRepository.save(
            createRepeatScheduleFromRequest(repeatScheduleCreateRequest, studyChannel));


        sendNotificationForScheduleCreate(studyChannelId, saveRepeatSchedule.getId(),
            saveRepeatSchedule.getScheduleDate(), NotificationType.SCHEDULE_CREATE,
            REPEAT_SCHEDULE_URL, REPEAT_SCHEDULE_CREATE);
    }

    public List<ScheduleResponse> getSchedulesInStudyChannel(
        Long memberId, Long studyChannelId) {
        validateStudyChannel(studyChannelId);
        validateStudyMember(memberId, studyChannelId);

        List<ScheduleResponse> scheduleResponses = new ArrayList<>();
        List<ScheduleResponse> singleScheduleResponses = singleScheduleRepository.findAllByStudyChannelId(
                studyChannelId)
            .stream()
            .map(SingleSchedule::toResponse)
            .collect(Collectors.toList());

        List<ScheduleResponse> repeatScheduleResponses = repeatScheduleRepository.findAllByStudyChannelId(
                studyChannelId)
            .stream()
            .map(RepeatSchedule::toResponse)
            .collect(Collectors.toList());

        scheduleResponses.addAll(singleScheduleResponses);
        scheduleResponses.addAll(repeatScheduleResponses);

        return scheduleResponses;
    }

    public List<ScheduleResponse> getSchedulesInStudyChannelForYearAndMonth(
        Long memberId, Long studyChannelId, int year, int month) {
        validateStudyChannel(studyChannelId);
        validateStudyMember(memberId, studyChannelId);

        List<ScheduleResponse> scheduleResponses = new ArrayList<>();

        LocalDate selectMonthFirstDate = LocalDate.of(year, month, 1);
        LocalDate selectMonthLastDate = selectMonthFirstDate.withDayOfMonth(selectMonthFirstDate.lengthOfMonth());
        List<ScheduleResponse> singleScheduleResponses = singleScheduleRepository.findAllByStudyChannelIdAndDateRange(
                studyChannelId,selectMonthFirstDate, selectMonthLastDate)
            .stream()
            .map(SingleSchedule::toResponse)
            .collect(Collectors.toList());

        List<ScheduleResponse> repeatScheduleResponses = repeatScheduleRepository.findAllByStudyChannelIdAndDate(
                studyChannelId, selectMonthFirstDate)
            .stream()
            .map(RepeatSchedule::toResponse)
            .collect(Collectors.toList());

        scheduleResponses.addAll(singleScheduleResponses);
        scheduleResponses.addAll(repeatScheduleResponses);
        return scheduleResponses;
    }

    public SingleSchedule getSingleSchedule(Long memberId, Long studyChannelId, Long scheduleId) {

        studyMemberRepository.findByMemberIdAndStudyChannelId(memberId, studyChannelId)
            .orElseThrow(NotStudyMemberException::new);

        return singleScheduleRepository.findById(scheduleId)
            .orElseThrow(NotFoundSingleScheduleException::new);
    }

    public RepeatSchedule getRepeatSchedule(Long memberId, Long studyChannelId, Long scheduleId) {

        studyMemberRepository.findByMemberIdAndStudyChannelId(memberId, studyChannelId)
            .orElseThrow(NotStudyMemberException::new);

        return repeatScheduleRepository.findById(scheduleId)
            .orElseThrow(NotFoundSingleScheduleException::new);
    }

    public void putSchedule(Long studyChannelId, ScheduleEditRequest scheduleEditRequest) {
        validateStudyChannel(studyChannelId);

        if (scheduleEditRequest instanceof SingleScheduleEditRequest) {
            SingleScheduleEditRequest editRequestToSingleSchedule =
                (SingleScheduleEditRequest) scheduleEditRequest;

            if(editRequestToSingleSchedule.getOriginType() != ScheduleType.SINGLE) {
                throw new IllegalArgumentForScheduleEditRequestException();
            }
            validateStudyLeader(editRequestToSingleSchedule.getMemberId(), studyChannelId);
            putScheduleSingleToSingle(editRequestToSingleSchedule);

        } else if (scheduleEditRequest instanceof RepeatScheduleEditRequest) {
            RepeatScheduleEditRequest editRequestToRepeatSchedule =
                (RepeatScheduleEditRequest) scheduleEditRequest;
            validateStudyLeader(editRequestToRepeatSchedule.getMemberId(), studyChannelId);

            if (editRequestToRepeatSchedule.getOriginType() == ScheduleType.SINGLE) {
                putScheduleSingleToRepeat(editRequestToRepeatSchedule);
            } else if (editRequestToRepeatSchedule.getOriginType() == ScheduleType.REPEAT) {
                putScheduleRepeatToRepeat(editRequestToRepeatSchedule);
            } else {
                throw new IllegalArgumentForScheduleEditRequestException();
            }

        } else {
            throw new IllegalArgumentForScheduleEditRequestException();
        }
    }

    public void putScheduleSingleToSingle(SingleScheduleEditRequest editRequestToSingleSchedule) {

        SingleSchedule singleSchedule = singleScheduleRepository.findById(
                editRequestToSingleSchedule.getScheduleId())
            .orElseThrow(NotFoundSingleScheduleException::new);

        singleSchedule.updateSingleSchedule(editRequestToSingleSchedule);
        singleScheduleRepository.save(singleSchedule);

        sendNotificationForScheduleUpdateOrDelete(singleSchedule.getStudyChannel().getId(),
            singleSchedule.getScheduleDate(), NotificationType.SCHEDULE_UPDATE,
            NotificationConstant.SCHEDULE_UPDATE_FOR_SINGLE_TO_SINGLE);
    }

    public void putScheduleSingleToRepeat(RepeatScheduleEditRequest editRequestToRepeatSchedule) {

        SingleSchedule singleSchedule = singleScheduleRepository.findById(
                editRequestToRepeatSchedule.getScheduleId())
            .orElseThrow(NotFoundSingleScheduleException::new);

        LocalDate selectedDate = editRequestToRepeatSchedule.getSelectedDate();
        RepeatCycle repeatCycle = editRequestToRepeatSchedule.getRepeatCycle();
        RepeatSituation repeatSituation = editRequestToRepeatSchedule.getRepeatSituation();
        validateRepeatSituation(selectedDate, repeatCycle, repeatSituation);

        repeatScheduleRepository.save(createRepeatScheduleFromRequest(
            editRequestToRepeatSchedule, singleSchedule.getStudyChannel()));

        singleScheduleRepository.deleteById(editRequestToRepeatSchedule.getScheduleId());

        sendNotificationForScheduleUpdateOrDelete(singleSchedule.getStudyChannel().getId(),
            singleSchedule.getScheduleDate(), NotificationType.SCHEDULE_UPDATE,
            NotificationConstant.SCHEDULE_UPDATE_FOR_SINGLE_TO_REPEAT);
    }

    public void putScheduleRepeatToRepeat(RepeatScheduleEditRequest editRequestToRepeatSchedule) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        RepeatSchedule repeatSchedule = repeatScheduleRepository.findById(
                editRequestToRepeatSchedule.getScheduleId())
            .orElseThrow(NotFoundRepeatScheduleException::new);

        if (editRequestToRepeatSchedule.getRepeatCycle() != repeatSchedule.getRepeatCycle()) {
            throw new IllegalArgumentForRepeatScheduleEditRequestException();
        }

        LocalDate selectedDate = editRequestToRepeatSchedule.getSelectedDate();
        RepeatCycle repeatCycle = editRequestToRepeatSchedule.getRepeatCycle();
        RepeatSituation repeatSituation = editRequestToRepeatSchedule.getRepeatSituation();
        validateRepeatSituation(selectedDate, repeatCycle, repeatSituation);

        if (currentDate.isEqual(editRequestToRepeatSchedule.getSelectedDate())) {
            // 기존 시작 일정이 현재 시간보다 이전이면 이미 출석처리가 됐을 것. 그러므로 변경 불가
            validateNotPastTime(repeatSchedule.getScheduleStartTime(), currentTime);
            // 당일 일정을 변경시에 현재 시간 보다 이전으로 시작 시간으로 변경하는 것은 안됨
            validateNotPastTime(editRequestToRepeatSchedule.getScheduleStartTime(), currentTime);
        }

        repeatSchedule.updateRepeatSchedule(editRequestToRepeatSchedule);
        repeatScheduleRepository.save(repeatSchedule);

        sendNotificationForScheduleUpdateOrDelete(repeatSchedule.getStudyChannel().getId(),
            repeatSchedule.getScheduleDate(), NotificationType.SCHEDULE_UPDATE,
            NotificationConstant.SCHEDULE_UPDATE_FOR_REPEAT_TO_REPEAT);
    }

    public void putScheduleRepeatToSingle(
        Long studyChannelId, Boolean isAfterEventSame, SingleScheduleEditRequest editRequestToSingleSchedule) {

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        validateStudyChannel(studyChannelId);

        RepeatSchedule repeatSchedule = repeatScheduleRepository.findById(
                editRequestToSingleSchedule.getScheduleId())
            .orElseThrow(NotFoundRepeatScheduleException::new);

        LocalDate selectedDate = editRequestToSingleSchedule.getSelectedDate();
        if (isNotIncluded(selectedDate, repeatSchedule.getScheduleDate(), repeatSchedule.getRepeatEndDate())) {
            throw new OutRangeScheduleException();
        }

        if (currentDate.isEqual(selectedDate)) {
            // 기존 시작 일정이 현재 시간보다 이전이면 이미 출석처리가 됐을 것. 그러므로 변경 불가
            validateNotPastTime(repeatSchedule.getScheduleStartTime(), currentTime);
            // 당일 일정을 변경시에 현재 시간 보다 이전으로 시작 시간으로 변경하는 것은 안됨
            validateNotPastTime(editRequestToSingleSchedule.getScheduleStartTime(), currentTime);
        }

        validateStudyLeader(editRequestToSingleSchedule.getMemberId(), studyChannelId);

        if (!isAfterEventSame) {
            putScheduleRepeatToSingleAfterEventNo(repeatSchedule, editRequestToSingleSchedule);

        } else if (isAfterEventSame) {
            putScheduleRepeatToSingleAfterEventYes(repeatSchedule, editRequestToSingleSchedule);

        } else {
            throw new IllegalArgumentForScheduleRequestException();
        }

        sendNotificationForScheduleUpdateOrDelete(repeatSchedule.getStudyChannel().getId(),
            repeatSchedule.getScheduleDate(), NotificationType.SCHEDULE_UPDATE,
            NotificationConstant.SCHEDULE_UPDATE_FOR_REPEAT_TO_SINGLE);
    }

    public void putScheduleRepeatToSingleAfterEventYes(RepeatSchedule repeatSchedule, SingleScheduleEditRequest singleScheduleEditRequest) {

        LocalDate selectedDate = singleScheduleEditRequest.getSelectedDate();
        if (selectedDate.equals(repeatSchedule.getScheduleDate())) {
            repeatScheduleRepository.deleteById(singleScheduleEditRequest.getScheduleId());
        } else if (isNextRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule.getScheduleDate())) {
            repeatScheduleRepository.deleteById(singleScheduleEditRequest.getScheduleId());
            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getScheduleDate()));

        } else {
            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        }

        // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
        if (repeatSchedule.getScheduleDate().equals(repeatSchedule.getRepeatEndDate())) {
            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getScheduleDate()));
            repeatScheduleRepository.deleteById(singleScheduleEditRequest.getScheduleId());
        }

        // 선택 날짜 single schedule
        singleScheduleRepository.save(createSingleScheduleFromRequest(
            singleScheduleEditRequest, repeatSchedule.getStudyChannel()));
    }

    public void putScheduleRepeatToSingleAfterEventNo(RepeatSchedule repeatSchedule, SingleScheduleEditRequest singleScheduleEditRequest) {

        LocalDate selectedDate = singleScheduleEditRequest.getSelectedDate();
        if (selectedDate.isEqual(repeatSchedule.getScheduleDate())) {
            // 기존 반복 일정: scheduleDate = scheduleDate + (주기 1)으로 변경
            changeRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        } else if (selectedDate.isEqual(repeatSchedule.getRepeatEndDate())) {
            // 기존 반복 일정: endDate = endDate - (주기 1)으로 변경
            changeRepeatEndDate(selectedDate,repeatSchedule.getRepeatCycle(), repeatSchedule);
        } else if (isNextRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule.getScheduleDate())) {
            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getScheduleDate()));
            changeRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);

        } else if (isFrontRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule.getRepeatEndDate())) {
            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getRepeatEndDate()));
            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);

        } else {
            RepeatSchedule secondRepeatSchedule =  makeAfterCycleRepeatSchedule(selectedDate, repeatSchedule);
            repeatScheduleRepository.save(secondRepeatSchedule);

            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        }

      // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
      if (repeatSchedule.getScheduleDate().isEqual(repeatSchedule.getRepeatEndDate())) {
        singleScheduleRepository.save(createSingleScheduleFromRepeat(
            repeatSchedule, repeatSchedule.getScheduleDate()));
        repeatScheduleRepository.deleteById(singleScheduleEditRequest.getScheduleId());
      }

        // 선택 날짜 single schedule
        singleScheduleRepository.save(createSingleScheduleFromRequest(
            singleScheduleEditRequest, repeatSchedule.getStudyChannel()));
    }

    public void deleteSingleSchedule(Long studyChannelId, ScheduleDeleteRequest scheduleDeleteRequest) {
        LocalDate currentDate =LocalDate.now();

        validateStudyChannel(studyChannelId);

        SingleSchedule singleSchedule = singleScheduleRepository.findById(
                scheduleDeleteRequest.getScheduleId())
            .orElseThrow(NotFoundSingleScheduleException::new);

        if (!scheduleDeleteRequest.getSelectedDate().isEqual(singleSchedule.getScheduleDate())) {
            throw new NotEqualSingleScheduleDate();
        }

        if (currentDate.isAfter(singleSchedule.getScheduleDate())) {
            throw new CanNotDeleteForBeforeDateException();
        }
        validateStudyLeader(scheduleDeleteRequest.getMemberId(), studyChannelId);
        singleScheduleRepository.deleteById(scheduleDeleteRequest.getScheduleId());

        sendNotificationForScheduleUpdateOrDelete(studyChannelId, scheduleDeleteRequest.getSelectedDate(),
            NotificationType.SCHEDULE_DELETE, SINGLE_SCHEDULE_DELETE);
    }

    public void deleteRepeatSchedule(Long studyChannelId, Boolean isAfterEventSame, ScheduleDeleteRequest scheduleDeleteRequest) {
        LocalDate currentDate =LocalDate.now();

        validateStudyChannel(studyChannelId);

        RepeatSchedule repeatSchedule = repeatScheduleRepository.findById(
                scheduleDeleteRequest.getScheduleId())
            .orElseThrow(NotFoundRepeatScheduleException::new);

        LocalDate selectedDate = scheduleDeleteRequest.getSelectedDate();

        if (isNotIncluded(selectedDate, repeatSchedule.getScheduleDate(), repeatSchedule.getRepeatEndDate())) {
            throw new OutRangeScheduleException();
        }

        if (currentDate.isAfter(repeatSchedule.getScheduleDate())) {
            throw new CanNotDeleteForBeforeDateException();
        }

        validateStudyLeader(scheduleDeleteRequest.getMemberId(), studyChannelId);

        if (isAfterEventSame) {
            deleteRepeatScheduleAfterEventSameYes(selectedDate, repeatSchedule);
        } else if (!isAfterEventSame) {
            deleteRepeatScheduleAfterEventSameNo(selectedDate, repeatSchedule);
        }

        sendNotificationForScheduleUpdateOrDelete(studyChannelId, scheduleDeleteRequest.getSelectedDate(),
            NotificationType.SCHEDULE_DELETE, REPEAT_SCHEDULE_DELETE);
    }

    public void deleteRepeatScheduleAfterEventSameYes(LocalDate selectedDate, RepeatSchedule repeatSchedule) {

        if (selectedDate.equals(repeatSchedule.getScheduleDate())) {
            // 선택 날짜 repeat schedule 삭제
            repeatScheduleRepository.deleteById(repeatSchedule.getId());
        } else if (isNextRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule.getScheduleDate())) {
            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getScheduleDate()));
            repeatScheduleRepository.deleteById(repeatSchedule.getId());
        } else {
            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        }
        // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
        if (repeatSchedule.getScheduleDate().equals(repeatSchedule.getRepeatEndDate())) {
            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getScheduleDate()));
            repeatScheduleRepository.deleteById(repeatSchedule.getId());
        }
    }

    public void deleteRepeatScheduleAfterEventSameNo(LocalDate selectedDate, RepeatSchedule repeatSchedule) {
        if (selectedDate.isEqual(repeatSchedule.getScheduleDate())) {
            // 기존 반복 일정: scheduleDate = scheduleDate + (주기 1)으로 변경
            changeRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        } else if (selectedDate.equals(repeatSchedule.getRepeatEndDate())) {
            // 기존 반복 일정: endDate = endDate - (주기 1)으로 변경
            changeRepeatEndDate(selectedDate,repeatSchedule.getRepeatCycle(), repeatSchedule);
        } else if (isNextRepeatStartDate(selectedDate,
            repeatSchedule.getRepeatCycle(), repeatSchedule.getScheduleDate())) {

            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getScheduleDate()));
            changeRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);

        } else if (isFrontRepeatEndDate(selectedDate,
            repeatSchedule.getRepeatCycle(), repeatSchedule.getRepeatEndDate())) {

            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getRepeatEndDate()));
            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);

        } else {
            RepeatSchedule secondRepeatSchedule = makeAfterCycleRepeatSchedule(selectedDate,  repeatSchedule);
            repeatScheduleRepository.save(secondRepeatSchedule);

            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        }

        // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
        if (repeatSchedule.getScheduleDate().isEqual(repeatSchedule.getRepeatEndDate())) {
            singleScheduleRepository.save(createSingleScheduleFromRepeat(
                repeatSchedule, repeatSchedule.getScheduleDate()));
            repeatScheduleRepository.deleteById(repeatSchedule.getId());
        }
    }

    private StudyChannel validateStudyChannel(Long studyChannelId){
        return studyChannelRepository.findById(studyChannelId)
            .orElseThrow(NotFoundStudyChannelException::new);
    }

    private void validateStudyMember(Long memberId, Long studyChannelId){
        studyMemberRepository.findByMemberIdAndStudyChannelId(memberId, studyChannelId)
            .orElseThrow(NotStudyMemberException::new);
    }

    private void validateStudyLeader(Long memberId, Long studyChannelId) {
        StudyMember studyMember = studyMemberRepository.findByMemberIdAndStudyChannelId(memberId,
                studyChannelId)
            .orElseThrow(NotStudyMemberException::new);

        if (!studyMember.isLeader()) {
            throw new NotStudyLeaderException();
        }
    }
  
    private boolean isNotIncluded(LocalDate selectedDate, LocalDate repeatStartDate
        , LocalDate repeatEndDate) {
        return (selectedDate.isAfter(repeatEndDate) || selectedDate.isBefore(repeatStartDate));
    }

    private boolean isNextRepeatStartDate(LocalDate selectedDate, RepeatCycle repeatCycle, LocalDate repeatStartDate) {
        return switch (repeatCycle) {
            case DAILY -> selectedDate.minusDays(1).isEqual(repeatStartDate);
            case WEEKLY -> selectedDate.minusWeeks(1).isEqual(repeatStartDate);
            case MONTHLY -> selectedDate.minusMonths(1).isEqual(repeatStartDate);
        };
    }

    private boolean isFrontRepeatEndDate(LocalDate selectedDate, RepeatCycle repeatCycle, LocalDate repeatEndDate) {
        return switch (repeatCycle) {
            case DAILY -> selectedDate.plusDays(1).isEqual(repeatEndDate);
            case WEEKLY -> selectedDate.plusWeeks(1).isEqual(repeatEndDate);
            case MONTHLY -> selectedDate.plusMonths(1).isEqual(repeatEndDate);
        };
    }

    private void changeRepeatStartDate(LocalDate selectedDate, RepeatCycle repeatCycle, RepeatSchedule repeatSchedule) {
        LocalDate newStartDate = switch (repeatCycle) {
            case DAILY -> selectedDate.plusDays(1);
            case WEEKLY -> selectedDate.plusWeeks(1);
            case MONTHLY -> selectedDate.plusMonths(1);
        };
        repeatSchedule.setRepeatStartDate(newStartDate);
        repeatScheduleRepository.save(repeatSchedule);
    }

    private void changeRepeatEndDate(LocalDate selectedDate, RepeatCycle repeatCycle, RepeatSchedule repeatSchedule) {
        LocalDate newEndDate = switch (repeatCycle) {
            case DAILY -> selectedDate.minusDays(1);
            case WEEKLY -> selectedDate.minusWeeks(1);
            case MONTHLY -> selectedDate.minusMonths(1);
        };
        repeatSchedule.setRepeatEndDate(newEndDate);
        repeatScheduleRepository.save(repeatSchedule);
    }

    private RepeatSchedule makeAfterCycleRepeatSchedule(LocalDate selectedDate, RepeatSchedule existRepeatSchedule) {
        LocalDate afterStartDate = switch (existRepeatSchedule.getRepeatCycle()) {
            case DAILY -> selectedDate.plusDays(1);
            case WEEKLY -> selectedDate.plusWeeks(1);
            case MONTHLY -> selectedDate.plusMonths(1);
        };

        return  RepeatSchedule.withoutIdBuilder()
            .scheduleName(existRepeatSchedule.getScheduleName())
            .scheduleContent(existRepeatSchedule.getScheduleContent())
            .scheduleDate(afterStartDate)
            .scheduleStartTime(existRepeatSchedule.getScheduleStartTime())
            .scheduleEndTime(existRepeatSchedule.getScheduleEndTime())
            .isRepeated(true)
            .studyChannel(existRepeatSchedule.getStudyChannel())
            .repeatSituation(existRepeatSchedule.getRepeatSituation())
            .repeatCycle(existRepeatSchedule.getRepeatCycle())
            .repeatEndDate(existRepeatSchedule.getRepeatEndDate())
            .build();
    }

    private void validateNotPastTime(LocalTime scheduleStartTime, LocalTime currentTime) {
        if (scheduleStartTime.isBefore(currentTime)) {
            throw new InvalidScheduleModificationException("당일의 일정을 변경할 경우 일정의 시작 시간 이전만 가능합니다.");
        }
    }

    private void validateRepeatSituation(LocalDate scheduleDate, RepeatCycle repeatCycle, RepeatSituation repeatSituation) {
        if (!isValidRepeatSituation(scheduleDate, repeatCycle, repeatSituation)) {
            throw new IllegalArgumentForRepeatSituationException();
        }
    }

    private boolean isValidRepeatSituation(LocalDate scheduleDate, RepeatCycle repeatCycle, RepeatSituation repeatSituation) {
        switch (repeatCycle) {
            case DAILY:
                return true; // DAILY 주기에서는 특별한 검증이 필요하지 않으므로 통과
            case WEEKLY:
                String name = scheduleDate.getDayOfWeek().name();
                return repeatSituation.name().equals(scheduleDate.getDayOfWeek().name());
            case MONTHLY:
                return (Integer) repeatSituation.getDescription() == scheduleDate.getDayOfMonth();
            default:
                throw new IllegalArgumentForScheduleRequestException();
        }
    }

    private RepeatSchedule createRepeatScheduleFromRequest(
        RepeatScheduleCreateRequest repeatScheduleCreateRequest, StudyChannel studyChannel) {
        return RepeatSchedule.withoutIdBuilder()
            .scheduleName(repeatScheduleCreateRequest.getScheduleName())
            .scheduleContent(repeatScheduleCreateRequest.getScheduleContent())
            .scheduleDate(repeatScheduleCreateRequest.getScheduleDate())
            .scheduleStartTime(repeatScheduleCreateRequest.getScheduleStartTime())
            .scheduleEndTime(repeatScheduleCreateRequest.getScheduleEndTime())
            .isRepeated(true)
            .repeatCycle(repeatScheduleCreateRequest.getRepeatCycle())
            .repeatSituation(repeatScheduleCreateRequest.getRepeatSituation())
            .repeatEndDate(repeatScheduleCreateRequest.getRepeatEndDate())
            .studyChannel(studyChannel)
            .placeId(repeatScheduleCreateRequest.getPlaceId())
            .build();
    }
    private RepeatSchedule createRepeatScheduleFromRequest(RepeatScheduleEditRequest editRequestToRepeatSchedule, StudyChannel studyChannel) {
        return RepeatSchedule.withoutIdBuilder()
            .scheduleName(editRequestToRepeatSchedule.getScheduleName())
            .scheduleContent(editRequestToRepeatSchedule.getScheduleContent())
            .scheduleContent(editRequestToRepeatSchedule.getScheduleContent())
            .scheduleDate(editRequestToRepeatSchedule.getSelectedDate())
            .scheduleStartTime(editRequestToRepeatSchedule.getScheduleStartTime())
            .scheduleEndTime(editRequestToRepeatSchedule.getScheduleEndTime())
            .isRepeated(true)
            .repeatEndDate(editRequestToRepeatSchedule.getRepeatEndDate())
            .repeatCycle(editRequestToRepeatSchedule.getRepeatCycle())
            .repeatSituation(editRequestToRepeatSchedule.getRepeatSituation())
            .studyChannel(studyChannel)
            .placeId(editRequestToRepeatSchedule.getPlaceId())
            .build();
    }

    private SingleSchedule createSingleScheduleFromRepeat(
        RepeatSchedule repeatSchedule, LocalDate repeatEndDate) {
        return SingleSchedule.withoutIdBuilder()
            .scheduleName(repeatSchedule.getScheduleName())
            .scheduleContent(repeatSchedule.getScheduleContent())
            .scheduleDate(repeatEndDate)
            .scheduleStartTime(repeatSchedule.getScheduleStartTime())
            .scheduleEndTime(repeatSchedule.getScheduleEndTime())
            .studyChannel(repeatSchedule.getStudyChannel())
            .placeId(repeatSchedule.getPlaceId())
            .isRepeated(false)
            .build();
    }

    private SingleSchedule createSingleScheduleFromRequest(SingleScheduleEditRequest singleScheduleEditRequest, StudyChannel studyChannel) {
        return SingleSchedule.withoutIdBuilder()
            .scheduleName(singleScheduleEditRequest.getScheduleName())
            .scheduleContent(singleScheduleEditRequest.getScheduleContent())
            .scheduleDate(singleScheduleEditRequest.getSelectedDate())
            .scheduleStartTime(singleScheduleEditRequest.getScheduleStartTime())
            .scheduleEndTime(singleScheduleEditRequest.getScheduleEndTime())
            .studyChannel(studyChannel)
            .placeId(singleScheduleEditRequest.getPlaceId())
            .isRepeated(false)
            .build();
    }

    private SingleSchedule createSingleScheduleFromRequest(SingleScheduleCreateRequest singleScheduleCreateRequest, StudyChannel studyChannel) {
        return SingleSchedule.withoutIdBuilder()
            .scheduleName(singleScheduleCreateRequest.getScheduleName())
            .scheduleContent(singleScheduleCreateRequest.getScheduleContent())
            .scheduleDate(singleScheduleCreateRequest.getScheduleDate())
            .scheduleStartTime(singleScheduleCreateRequest.getScheduleStartTime())
            .scheduleEndTime(singleScheduleCreateRequest.getScheduleEndTime())
            .studyChannel(studyChannel)
            .placeId(singleScheduleCreateRequest.getPlaceId())
            .isRepeated(false)
            .build();
    }

    private void sendNotificationForScheduleCreate(Long studyChannelId, Long scheduleId,
        LocalDate scheduleDate, NotificationType notificationType,
        String relateUrlFormat, String notificationFormatMessage) {
        // 로그 메시지 및 알림 메시지에 사용할 날짜 포맷터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
        // 단일 일정 날짜를 포맷팅
        String formattedDate = scheduleDate.format(formatter);
        // 알림 메시지 생성
        String notificationMessage = String.format(notificationFormatMessage, studyChannelId, formattedDate);
        // 관련 URL 생성
        String relateUrl = String.format(relateUrlFormat, studyChannelId, scheduleId);
        notificationService.sendNotificationToStudyChannel(studyChannelId,
            notificationType, notificationMessage, relateUrl);
        // 로그 메시지
        log.info(notificationMessage);
    }


    private void sendNotificationForScheduleUpdateOrDelete(Long studyChannelId, LocalDate scheduleDate,
        NotificationType notificationType, String notificationFormatMessage) {
        // 로그 메시지 및 알림 메시지에 사용할 날짜 포맷터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
        // 단일 일정 날짜를 포맷팅
        String formattedDate = scheduleDate.format(formatter);
        // 알림 메시지 생성
        String notificationMessage = String.format(notificationFormatMessage, studyChannelId, formattedDate);
        // 관련 URL 생성
        int scheduleYear = scheduleDate.getYear();
        int scheduleMonth = scheduleDate.getMonthValue();
        String relateUrl = String.format("/api/study-channels/%d/schedules/date?year=%d&month=%d",
            studyChannelId, scheduleYear, scheduleMonth);
        notificationService.sendNotificationToStudyChannel(studyChannelId,
            notificationType, notificationMessage, relateUrl);
        // 로그 메시지
        log.info(notificationMessage);
    }
}
