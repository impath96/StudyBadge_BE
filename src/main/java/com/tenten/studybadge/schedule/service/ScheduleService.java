package com.tenten.studybadge.schedule.service;

import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForRepeatScheduleEditRequestException;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleRequestException;
import com.tenten.studybadge.common.exception.schedule.NotFoundRepeatScheduleException;
import com.tenten.studybadge.common.exception.schedule.NotFoundSingleScheduleException;
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
}
