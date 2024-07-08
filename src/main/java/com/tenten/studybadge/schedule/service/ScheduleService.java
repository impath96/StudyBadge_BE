package com.tenten.studybadge.schedule.service;

import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleRequestException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.ScheduleRepository;
import com.tenten.studybadge.schedule.dto.RepeatScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.ScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.SingleScheduleCreateRequest;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {
  private final ScheduleRepository<SingleSchedule> singleScheduleRepository;
  private final ScheduleRepository<RepeatSchedule> repeatScheduleRepository;
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
}
