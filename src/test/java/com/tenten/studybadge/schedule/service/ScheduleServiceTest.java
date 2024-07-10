package com.tenten.studybadge.schedule.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.tenten.studybadge.common.exception.studychannel.InvalidStudyDurationException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.RepeatScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.ScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.SingleScheduleRepository;
import com.tenten.studybadge.schedule.dto.RepeatScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.ScheduleResponse;
import com.tenten.studybadge.schedule.dto.SingleScheduleCreateRequest;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ScheduleServiceTest {
  @Mock
  private SingleScheduleRepository singleScheduleRepository;
  @Mock
  private RepeatScheduleRepository repeatScheduleRepository;
  @Mock
  private StudyChannelRepository studyChannelRepository;

  @InjectMocks
  private ScheduleService scheduleService;

  private StudyChannel studyChannel;
  private SingleSchedule singleScheduleWithoutPlace;
  private RepeatSchedule repeatScheduleWithoutPlace;
  private SingleSchedule singleScheduleWithPlace;
  private RepeatSchedule repeatScheduleWithPlace;

  @BeforeEach
  public void setUp() {
    scheduleService = new ScheduleService(
        singleScheduleRepository, repeatScheduleRepository, studyChannelRepository);
    studyChannel = StudyChannel.builder()
        .id(1L)
        .name("test study channel1")
        .build();

    singleScheduleWithoutPlace = SingleSchedule.withoutIdBuilder()
        .scheduleName("Single Meeting")
        .scheduleContent("Content for single meeting")
        .scheduleDate(LocalDate.of(2024, 7, 5))
        .scheduleStartTime(LocalTime.of(10, 0))
        .scheduleEndTime(LocalTime.of(11, 0))
        .isRepeated(false)
        .studyChannel(studyChannel)
        .placeId(null)
        .build();

    repeatScheduleWithoutPlace =  RepeatSchedule.withoutIdBuilder()
        .scheduleName("Repeat Meeting")
        .scheduleContent("Content for repeat meeting")
        .scheduleDate(LocalDate.of(2024, 7, 5))
        .scheduleStartTime(LocalTime.of(10, 0))
        .scheduleEndTime(LocalTime.of(11, 0))
        .repeatCycle(RepeatCycle.WEEKLY)
        .repeatSituation(RepeatSituation.MONDAY)
        .repeatEndDate(LocalDate.of(2024, 12, 31))
        .isRepeated(true)
        .studyChannel(studyChannel)
        .placeId(null)
        .build();

    singleScheduleWithPlace = SingleSchedule.withoutIdBuilder()
        .scheduleName("Single Meeting")
        .scheduleContent("Content for single meeting")
        .scheduleDate(LocalDate.of(2024, 7, 5))
        .scheduleStartTime(LocalTime.of(10, 0))
        .scheduleEndTime(LocalTime.of(11, 0))
        .isRepeated(false)
        .studyChannel(studyChannel)
        .placeId(1L)
        .build();

    repeatScheduleWithPlace =  RepeatSchedule.withoutIdBuilder()
        .scheduleName("Repeat Meeting")
        .scheduleContent("Content for repeat meeting")
        .scheduleDate(LocalDate.of(2024, 7, 5))
        .scheduleStartTime(LocalTime.of(10, 0))
        .scheduleEndTime(LocalTime.of(11, 0))
        .repeatCycle(RepeatCycle.WEEKLY)
        .repeatSituation(RepeatSituation.MONDAY)
        .repeatEndDate(LocalDate.of(2024, 12, 31))
        .isRepeated(true)
        .studyChannel(studyChannel)
        .placeId(1L)
        .build();
  }

  @Test
  @DisplayName("단순 일정 등록 성공 - 장소 정보가 없을 때")
  public void testPostSingleSchedule() {
    // given
    SingleScheduleCreateRequest singleScheduleRequestWithoutPlace = new SingleScheduleCreateRequest(
        "Single Meeting",
        "Content for single meeting",
        LocalDate.of(2024, 7, 5),
        LocalTime.of(10, 0),
        LocalTime.of( 11, 0),
        null
    );

    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
    given(singleScheduleRepository.save(any(SingleSchedule.class))).willReturn(singleScheduleWithoutPlace);

    // when
    scheduleService.postSchedule(singleScheduleRequestWithoutPlace, 1L);

    // then
    verify(singleScheduleRepository, times(1)).save(any(SingleSchedule.class));
    verify(repeatScheduleRepository, times(0)).save(any(RepeatSchedule.class));
  }

  @Test
  @DisplayName("반복 일정 등록 성공 - 장소 정보가 없을 때")
  public void testPostRepeatSchedule() {
    // given
    RepeatScheduleCreateRequest repeatScheduleRequestWithoutPlace = new RepeatScheduleCreateRequest(
        "Weekly Meeting",
        "Content for weekly meeting",
        LocalDate.of(2024, 7, 5),
        LocalTime.of(10, 0),
        LocalTime.of( 11, 0),
        RepeatCycle.WEEKLY,
        RepeatSituation.MONDAY,
        LocalDate.of(2024, 12, 31),
        null
    );

    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
    given(repeatScheduleRepository.save(any(RepeatSchedule.class))).willReturn(repeatScheduleWithoutPlace);

    // when
    scheduleService.postSchedule(repeatScheduleRequestWithoutPlace, 1L);

    // then
    verify(repeatScheduleRepository, times(1)).save(any(RepeatSchedule.class));
    verify(singleScheduleRepository, times(0)).save(any(SingleSchedule.class));
  }

  @Test
  @DisplayName("단순 일정 등록 성공 - 장소 정보가 있을 때")
  public void testPostSingleSchedule_WithPlace() {
    // given
    SingleScheduleCreateRequest singleScheduleRequestWithPlace = new SingleScheduleCreateRequest(
        "Single Meeting",
        "Content for single meeting",
        LocalDate.of(2024, 7, 5),
        LocalTime.of(10, 0),
        LocalTime.of( 11, 0),
        1L
    );

    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
    given(singleScheduleRepository.save(any(SingleSchedule.class))).willReturn(singleScheduleWithPlace);

    // when
    scheduleService.postSchedule(singleScheduleRequestWithPlace, 1L);

    // then
    verify(singleScheduleRepository, times(1)).save(any(SingleSchedule.class));
    verify(repeatScheduleRepository, times(0)).save(any(RepeatSchedule.class));
  }

  @Test
  @DisplayName("반복 일정 등록 성공 - 장소 정보가 있을 때")
  public void testPostRepeatSchedule_WithPlace() {
    // given
    RepeatScheduleCreateRequest repeatScheduleRequestWithPlace = new RepeatScheduleCreateRequest(
        "Weekly Meeting",
        "Content for weekly meeting",
        LocalDate.of(2024, 7, 5),
        LocalTime.of(10, 0),
        LocalTime.of( 11, 0),
        RepeatCycle.WEEKLY,
        RepeatSituation.MONDAY,
        LocalDate.of(2024, 12, 31),
        1L
    );

    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
    given(repeatScheduleRepository.save(any(RepeatSchedule.class))).willReturn(repeatScheduleWithPlace);

    // when
    scheduleService.postSchedule(repeatScheduleRequestWithPlace, 1L);

    // then
    verify(repeatScheduleRepository, times(1)).save(any(RepeatSchedule.class));
    verify(singleScheduleRepository, times(0)).save(any(SingleSchedule.class));
  }


  @Test
  @DisplayName("스터디 채널 내의 일정 전체 조회 성공")
  public void success_testGetSchedulesInStudyChannel() {
    // given
    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
    given(singleScheduleRepository.findAllByStudyChannelId(1L))
        .willReturn(Arrays.asList(singleScheduleWithoutPlace));
    given(repeatScheduleRepository.findAllByStudyChannelId(1L))
        .willReturn(Arrays.asList(repeatScheduleWithoutPlace));

    // when
    List<ScheduleResponse> scheduleResponses = scheduleService.getSchedulesInStudyChannel(1L);

    // then
    assertEquals(2, scheduleResponses.size());
    verify(studyChannelRepository, times(1)).findById(1L);
    verify(singleScheduleRepository, times(1)).findAllByStudyChannelId(1L);
    verify(repeatScheduleRepository, times(1)).findAllByStudyChannelId(1L);
  }

  @Test
  @DisplayName("스터디 채널 내의 일정 yyyy.mm 기준 전체 조회 성공")
  public void success_testGetSchedulesInStudyChannelByYearAndMonth() {
    // given
    RepeatSchedule repeatSchedule2 = RepeatSchedule.withoutIdBuilder()
        .scheduleDate(LocalDate.of(2024, 5, 15))
        .repeatEndDate(LocalDate.of(2024, 9, 15))
        .studyChannel(studyChannel)
        .build();
    LocalDate selectMonthFirstDate = LocalDate.of(2024, 7, 1);
    LocalDate selectMonthLastDate = selectMonthFirstDate.withDayOfMonth(selectMonthFirstDate.lengthOfMonth());

    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
    given(singleScheduleRepository.findAllByStudyChannelIdAndDateRange(
        1L, selectMonthFirstDate, selectMonthLastDate))
        .willReturn(Arrays.asList(singleScheduleWithoutPlace));

    given(repeatScheduleRepository.findAllByStudyChannelIdAndDate(
        1L, selectMonthFirstDate))
        .willReturn(Arrays.asList(repeatSchedule2));

    // when
    List<ScheduleResponse> scheduleResponses = scheduleService.getSchedulesInStudyChannelForYearAndMonth(
        1L, 2024, 7);

    // then
    assertEquals(2, scheduleResponses.size());
    verify(studyChannelRepository, times(1)).findById(1L);
    verify(singleScheduleRepository, times(1)).findAllByStudyChannelIdAndDateRange(
        1L, selectMonthFirstDate, selectMonthLastDate);
    verify(repeatScheduleRepository, times(1)).findAllByStudyChannelIdAndDate(
        1L, selectMonthFirstDate);
  }

  @Test
  @DisplayName("스터디 채널 내의 일정 전체 조회 실패: study channel이 존재하지 않을 때")
  public void fail_testGetSchedulesInStudyChannel() {
    // given
    given(studyChannelRepository.findById(1L)).willReturn(Optional.empty());

    // when & then
    assertThrows(NotFoundStudyChannelException.class, () -> {
      scheduleService.getSchedulesInStudyChannel(1L);
    });

    // then
    verify(studyChannelRepository, times(1)).findById(1L);
    verify(singleScheduleRepository, times(0)).findAllByStudyChannelId(1L);
    verify(repeatScheduleRepository, times(0)).findAllByStudyChannelId(1L);
  }
}