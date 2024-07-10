package com.tenten.studybadge.schedule.service;

import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForRepeatScheduleEditRequestException;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleRequestException;
import com.tenten.studybadge.common.exception.schedule.NotFoundRepeatScheduleException;
import com.tenten.studybadge.common.exception.schedule.NotFoundSingleScheduleException;
import com.tenten.studybadge.common.exception.schedule.OutRangeScheduleException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.RepeatScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.SingleScheduleRepository;
import com.tenten.studybadge.schedule.dto.RepeatScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.RepeatScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.ScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleResponse;
import com.tenten.studybadge.schedule.dto.SingleScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.SingleScheduleEditRequest;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.ScheduleOriginType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {
  private final SingleScheduleRepository singleScheduleRepository;
  private final RepeatScheduleRepository repeatScheduleRepository;
  private final StudyChannelRepository studyChannelRepository;

  public void postSchedule(ScheduleCreateRequest scheduleCreateRequest, Long studyChannelId) {
    StudyChannel studyChannel =  studyChannelRepository.findById(studyChannelId)
        .orElseThrow(NotFoundStudyChannelException::new);

    // type: repeat
    if (scheduleCreateRequest instanceof RepeatScheduleCreateRequest) {
      RepeatScheduleCreateRequest repeatRequest = (RepeatScheduleCreateRequest) scheduleCreateRequest;

      repeatScheduleRepository.save(RepeatSchedule.withoutIdBuilder()
              .scheduleName(repeatRequest.getScheduleName())
              .scheduleContent(repeatRequest.getScheduleContent())
              .scheduleDate(repeatRequest.getScheduleDate())
              .scheduleStartTime(repeatRequest.getScheduleStartTime())
              .scheduleEndTime(repeatRequest.getScheduleEndTime())
              .isRepeated(true)
              .repeatCycle(repeatRequest.getRepeatCycle())
              .repeatSituation(repeatRequest.getRepeatSituation())
              .repeatEndDate(repeatRequest.getRepeatEndDate())
              .studyChannel(studyChannel)
              .placeId(repeatRequest.getPlaceId())
          .build());
    }
    // type: single
    else if (scheduleCreateRequest instanceof SingleScheduleCreateRequest) {
      SingleScheduleCreateRequest singleRequest = (SingleScheduleCreateRequest) scheduleCreateRequest;

      singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
          .scheduleName(singleRequest.getScheduleName())
          .scheduleContent(singleRequest.getScheduleContent())
          .scheduleDate(singleRequest.getScheduleDate())
          .scheduleStartTime(singleRequest.getScheduleStartTime())
          .scheduleEndTime(singleRequest.getScheduleEndTime())
          .isRepeated(false)
          .studyChannel(studyChannel)
          .placeId(singleRequest.getPlaceId())
          .build());
    } else {
      throw new IllegalArgumentForScheduleRequestException();
    }
  }

  public List<ScheduleResponse> getSchedulesInStudyChannel(Long studyChannelId) {
    StudyChannel studyChannel = studyChannelRepository.findById(studyChannelId)
        .orElseThrow(NotFoundStudyChannelException::new);

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

  public List<ScheduleResponse> getSchedulesInStudyChannelForYearAndMonth(Long studyChannelId, int year, int month) {
    StudyChannel studyChannel = studyChannelRepository.findById(studyChannelId)
        .orElseThrow(NotFoundStudyChannelException::new);

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

  public void putSchedule(Long studyChannelId, ScheduleEditRequest scheduleEditRequest) {
    StudyChannel studyChannel = studyChannelRepository.findById(studyChannelId)
        .orElseThrow(NotFoundStudyChannelException::new);

    if (scheduleEditRequest.getOriginType() == ScheduleOriginType.SINGLE) {
      SingleSchedule singleSchedule = singleScheduleRepository.findById(
              scheduleEditRequest.getScheduleId())
          .orElseThrow(NotFoundSingleScheduleException::new);

      if (scheduleEditRequest instanceof SingleScheduleEditRequest) {
        putScheduleSingleToSingle(
            singleSchedule, (SingleScheduleEditRequest) scheduleEditRequest);
      } else if (scheduleEditRequest instanceof RepeatScheduleEditRequest) {
        putScheduleSingleToRepeat(
            singleSchedule, (RepeatScheduleEditRequest) scheduleEditRequest);
      }
    } else if (scheduleEditRequest.getOriginType() == ScheduleOriginType.REPEAT) {
      RepeatSchedule repeatSchedule = repeatScheduleRepository.findById(
              scheduleEditRequest.getScheduleId())
          .orElseThrow(NotFoundRepeatScheduleException::new);

      if (scheduleEditRequest instanceof RepeatScheduleEditRequest) {
        putScheduleRepeatToRepeat(
            repeatSchedule, (RepeatScheduleEditRequest) scheduleEditRequest);
      }
    } else {
      throw new IllegalArgumentForScheduleRequestException();
    }
  }

  public void putScheduleSingleToSingle(SingleSchedule singleSchedule, SingleScheduleEditRequest singleScheduleEditRequest) {
      singleSchedule.updateSingleSchedule(singleScheduleEditRequest);
      singleScheduleRepository.save(singleSchedule);
  }

  public void putScheduleSingleToRepeat(SingleSchedule singleSchedule, RepeatScheduleEditRequest repeatScheduleEditRequest) {

    repeatScheduleRepository.save(RepeatSchedule.withoutIdBuilder()
        .scheduleName(repeatScheduleEditRequest.getScheduleName())
        .scheduleContent(repeatScheduleEditRequest.getScheduleContent())
        .scheduleContent(repeatScheduleEditRequest.getScheduleContent())
        .scheduleDate(repeatScheduleEditRequest.getSelectedDate())
        .scheduleStartTime(repeatScheduleEditRequest.getScheduleStartTime())
        .scheduleEndTime(repeatScheduleEditRequest.getScheduleEndTime())
        .isRepeated(true)
        .repeatEndDate(repeatScheduleEditRequest.getRepeatEndDate())
        .repeatCycle(repeatScheduleEditRequest.getRepeatCycle())
        .repeatSituation(repeatScheduleEditRequest.getRepeatSituation())
        .studyChannel(singleSchedule.getStudyChannel())
        .placeId(repeatScheduleEditRequest.getPlaceId())
        .build());
    singleScheduleRepository.deleteById(repeatScheduleEditRequest.getScheduleId());
  }

  public void putScheduleRepeatToRepeat(RepeatSchedule repeatSchedule, RepeatScheduleEditRequest repeatScheduleEditRequest) {

    if (repeatSchedule.getRepeatCycle() != repeatScheduleEditRequest.getRepeatCycle()) {
      throw new IllegalArgumentForRepeatScheduleEditRequestException();
    }

    repeatSchedule.updateRepeatSchedule(repeatScheduleEditRequest);
    repeatScheduleRepository.save(repeatSchedule);
  }

  public void putRepeatScheduleWithAfterEventSame(
      Long studyChannelId, Boolean isAfterEventSame, ScheduleEditRequest scheduleEditRequest) {

    StudyChannel studyChannel = studyChannelRepository.findById(studyChannelId)
        .orElseThrow(NotFoundStudyChannelException::new);

    RepeatSchedule repeatSchedule = repeatScheduleRepository.findById(
            scheduleEditRequest.getScheduleId())
        .orElseThrow(NotFoundRepeatScheduleException::new);

    if (isNotIncluded(scheduleEditRequest.getSelectedDate(), repeatSchedule.getScheduleDate(), repeatSchedule.getRepeatEndDate())) {
      throw new OutRangeScheduleException();
    }

    if (scheduleEditRequest.getOriginType() == ScheduleOriginType.REPEAT
    && !isAfterEventSame) {
      putScheduleRepeatToSingleAfterEventNo(repeatSchedule, (SingleScheduleEditRequest) scheduleEditRequest);

    } else if (scheduleEditRequest.getOriginType() == ScheduleOriginType.REPEAT
        && isAfterEventSame) {
      putScheduleRepeatToSingleAfterEventYes(repeatSchedule, (SingleScheduleEditRequest) scheduleEditRequest);

    } else {
      throw new IllegalArgumentForScheduleRequestException();
    }
  }

  public void putScheduleRepeatToSingleAfterEventYes(RepeatSchedule repeatSchedule, SingleScheduleEditRequest singleScheduleEditRequest) {

    LocalDate selectedDate = singleScheduleEditRequest.getSelectedDate();
    if (selectedDate.equals(repeatSchedule.getScheduleDate())) {
      repeatScheduleRepository.deleteById(repeatSchedule.getId());
    } else if (isNextRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule.getScheduleDate())) {
        repeatScheduleRepository.deleteById(singleScheduleEditRequest.getScheduleId());
        singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
            .scheduleName(repeatSchedule.getScheduleName())
            .scheduleContent(repeatSchedule.getScheduleContent())
            .scheduleDate(repeatSchedule.getScheduleDate())
            .scheduleStartTime(repeatSchedule.getScheduleStartTime())
            .scheduleEndTime(repeatSchedule.getScheduleEndTime())
            .studyChannel(repeatSchedule.getStudyChannel())
            .placeId(repeatSchedule.getPlaceId())
            .isRepeated(false)
            .build());
    }

    changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);

    // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
    if (repeatSchedule.getScheduleDate().equals(repeatSchedule.getRepeatEndDate())) {
      SingleSchedule.withoutIdBuilder()
          .scheduleName(repeatSchedule.getScheduleName())
          .scheduleContent(repeatSchedule.getScheduleContent())
          .scheduleDate(repeatSchedule.getScheduleDate())
          .scheduleStartTime(repeatSchedule.getScheduleStartTime())
          .scheduleEndTime(repeatSchedule.getScheduleEndTime())
          .isRepeated(false)
          .studyChannel(repeatSchedule.getStudyChannel())
          .placeId(repeatSchedule.getPlaceId())
          .build();
      repeatScheduleRepository.deleteById(repeatSchedule.getId());
    }

    // 선택 날짜 single schedule
    singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
        .scheduleName(singleScheduleEditRequest.getScheduleName())
        .scheduleContent(singleScheduleEditRequest.getScheduleContent())
        .scheduleDate(selectedDate)
        .scheduleStartTime(singleScheduleEditRequest.getScheduleStartTime())
        .scheduleEndTime(singleScheduleEditRequest.getScheduleEndTime())
        .isRepeated(false)
        .studyChannel(repeatSchedule.getStudyChannel())
        .placeId(singleScheduleEditRequest.getPlaceId())
        .build());
  }

  public void putScheduleRepeatToSingleAfterEventNo(RepeatSchedule repeatSchedule, SingleScheduleEditRequest singleScheduleEditRequest) {

    LocalDate selectedDate = singleScheduleEditRequest.getSelectedDate();
    if (selectedDate.equals(repeatSchedule.getScheduleDate())) {
      // 기존 반복 일정: scheduleDate = scheduleDate + (주기 1)으로 변경
      changeRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
    } else if (selectedDate.equals(repeatSchedule.getRepeatEndDate())) {
      // 기존 반복 일정: endDate = endDate - (주기 1)으로 변경
      changeRepeatEndDate(selectedDate,repeatSchedule.getRepeatCycle(), repeatSchedule);
    } else if (isNextRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule.getScheduleDate())) {
        singleScheduleRepository.save(
            SingleSchedule.withoutIdBuilder()
                .scheduleName(repeatSchedule.getScheduleName())
                .scheduleContent(repeatSchedule.getScheduleContent())
                .scheduleDate(repeatSchedule.getScheduleDate())
                .scheduleStartTime(repeatSchedule.getScheduleStartTime())
                .scheduleEndTime(repeatSchedule.getScheduleEndTime())
                .isRepeated(false)
                .studyChannel(repeatSchedule.getStudyChannel())
                .placeId(repeatSchedule.getPlaceId())
                .build()
        );
        changeRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);

    } else if (isFrontRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule.getRepeatEndDate())) {
      singleScheduleRepository.save(
          SingleSchedule.withoutIdBuilder()
              .scheduleName(repeatSchedule.getScheduleName())
              .scheduleContent(repeatSchedule.getScheduleContent())
              .scheduleDate(repeatSchedule.getRepeatEndDate()) // 반복 마지막 날짜로 단일 일정이된다.
              .scheduleStartTime(repeatSchedule.getScheduleStartTime())
              .scheduleEndTime(repeatSchedule.getScheduleEndTime())
              .isRepeated(false)
              .studyChannel(repeatSchedule.getStudyChannel())
              .placeId(repeatSchedule.getPlaceId())
              .build()
      );
      changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);

    } else {
      RepeatSchedule secondRepeatSchedule =  makeAfterCycleRepeatSchedule(selectedDate,  repeatSchedule);
      changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);

      repeatScheduleRepository.save(secondRepeatSchedule);
    }

    // 선택 날짜 single schedule
    singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
        .scheduleName(singleScheduleEditRequest.getScheduleName())
        .scheduleContent(singleScheduleEditRequest.getScheduleContent())
        .scheduleDate(selectedDate)
        .scheduleStartTime(singleScheduleEditRequest.getScheduleStartTime())
        .scheduleEndTime(singleScheduleEditRequest.getScheduleEndTime())
        .isRepeated(false)
        .studyChannel(repeatSchedule.getStudyChannel())
        .placeId(singleScheduleEditRequest.getPlaceId())
        .build());
  }

  private boolean isNotIncluded(LocalDate selectedDate, LocalDate repeatStartDate, LocalDate repeatEndDate) {
    return (selectedDate.isAfter(repeatEndDate) || selectedDate.isBefore(repeatStartDate));
  }

  private boolean isNextRepeatStartDate(LocalDate selectedDate, RepeatCycle repeatCycle, LocalDate repeatStartDate) {
    switch (repeatCycle) {
      case DAILY:
        return selectedDate.minusDays(1).isEqual(repeatStartDate);
      case WEEKLY:
        return selectedDate.minusWeeks(1).isEqual(repeatStartDate);
      case MONTHLY:
        return selectedDate.minusMonths(1).isEqual(repeatStartDate);
    }
    return false;
  }

  private boolean isFrontRepeatEndDate(LocalDate selectedDate, RepeatCycle repeatCycle, LocalDate repeatEndDate) {
    switch (repeatCycle) {
      case DAILY:
        return selectedDate.plusDays(1).isEqual(repeatEndDate);
      case WEEKLY:
        return selectedDate.plusWeeks(1).isEqual(repeatEndDate);
      case MONTHLY:
        return selectedDate.plusMonths(1).isEqual(repeatEndDate);
    }
    return false;
  }

  private void changeRepeatStartDate(LocalDate selectedDate, RepeatCycle repeatCycle
      , RepeatSchedule repeatSchedule) {
    switch (repeatCycle) {
      case DAILY:
        repeatSchedule.setRepeatStartDate(selectedDate.plusDays(1));
        break;
      case WEEKLY:
        repeatSchedule.setRepeatStartDate(selectedDate.plusWeeks(1));
        break;
      case MONTHLY:
        repeatSchedule.setRepeatStartDate(selectedDate.plusMonths(1));
        break;
    }
  }

  private void changeRepeatEndDate(LocalDate selectedDate, RepeatCycle repeatCycle
      , RepeatSchedule repeatSchedule) {
    switch (repeatCycle) {
      case DAILY:
        repeatSchedule.setRepeatEndDate(selectedDate.minusDays(1));
        break;
      case WEEKLY:
        repeatSchedule.setRepeatEndDate(selectedDate.minusWeeks(1));
        break;
      case MONTHLY:
        repeatSchedule.setRepeatEndDate(selectedDate.minusMonths(1));
        break;
    }
  }

  private RepeatSchedule makeAfterCycleRepeatSchedule(LocalDate selectedDate, RepeatSchedule existRepeatSchedule) {
    LocalDate afterStartDate = null;

    switch (existRepeatSchedule.getRepeatCycle()) {
      case DAILY:
        afterStartDate = selectedDate.plusDays(1);
        break;
      case WEEKLY:
        afterStartDate = selectedDate.plusWeeks(1);
        break;
      case MONTHLY:
        afterStartDate = selectedDate.plusMonths(1);
        break;
    }

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
}
