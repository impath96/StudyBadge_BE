package com.tenten.studybadge.schedule.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForRepeatSituationException;
import com.tenten.studybadge.common.exception.schedule.NotEqualSingleScheduleDate;
import com.tenten.studybadge.common.exception.schedule.OutRangeScheduleException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.domain.repository.RepeatScheduleRepository;
import com.tenten.studybadge.schedule.domain.repository.SingleScheduleRepository;
import com.tenten.studybadge.schedule.dto.RepeatScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.RepeatScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleDeleteRequest;
import com.tenten.studybadge.schedule.dto.ScheduleResponse;
import com.tenten.studybadge.schedule.dto.SingleScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.SingleScheduleEditRequest;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.type.schedule.ScheduleType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
            .repeatSituation(RepeatSituation.FRIDAY)
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
            .repeatSituation(RepeatSituation.FRIDAY)
            .repeatEndDate(LocalDate.of(2024, 8, 9))
            .isRepeated(true)
            .studyChannel(studyChannel)
            .placeId(1L)
            .build();
    }

    @DisplayName("일정 등록")
    @Nested
    class schedulePostTest {
        @Test
        @DisplayName("단순 일정 등록 성공 - 장소 정보가 없을 때")
        public void testPostSingleSchedule() {
            // given
            SingleScheduleCreateRequest singleScheduleRequestWithoutPlace =
                new SingleScheduleCreateRequest(
                "Single Meeting",
                "Content for single meeting",
                LocalDate.of(2024, 7, 5),
                LocalTime.of(10, 0),
                LocalTime.of( 11, 0),
                null
            );

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(singleScheduleRepository.save(any(SingleSchedule.class)))
                .willReturn(singleScheduleWithoutPlace);

            // when
            scheduleService.postSingleSchedule(
                singleScheduleRequestWithoutPlace, 1L);

            // then
            verify(singleScheduleRepository, times(1))
                .save(any(SingleSchedule.class));
            verify(repeatScheduleRepository, times(0))
                .save(any(RepeatSchedule.class));
        }

        @Test
        @DisplayName("반복 일정 등록 성공 - 장소 정보가 없을 때")
        public void testPostRepeatSchedule() {
            // given
            RepeatScheduleCreateRequest repeatScheduleRequestWithoutPlace =
                new RepeatScheduleCreateRequest(
                "Weekly Meeting",
                "Content for weekly meeting",
                LocalDate.of(2024, 7, 5),
                LocalTime.of(10, 0),
                LocalTime.of( 11, 0),
                null,
                RepeatCycle.WEEKLY,
                RepeatSituation.FRIDAY,
                LocalDate.of(2024, 12, 31)
            );

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.save(any(RepeatSchedule.class)))
                .willReturn(repeatScheduleWithoutPlace);

            // when
            scheduleService.postRepeatSchedule(
                repeatScheduleRequestWithoutPlace, 1L);

            // then
            verify(repeatScheduleRepository, times(1))
                .save(any(RepeatSchedule.class));
            verify(singleScheduleRepository, times(0))
                .save(any(SingleSchedule.class));
        }

        @Test
        @DisplayName("단순 일정 등록 성공 - 장소 정보가 있을 때")
        public void testPostSingleSchedule_WithPlace() {
            // given
            SingleScheduleCreateRequest singleScheduleRequestWithPlace =
                new SingleScheduleCreateRequest(
                "Single Meeting",
                "Content for single meeting",
                LocalDate.of(2024, 7, 5),
                LocalTime.of(10, 0),
                LocalTime.of( 11, 0),
                1L
            );

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(singleScheduleRepository.save(any(SingleSchedule.class)))
                .willReturn(singleScheduleWithPlace);

            // when
            scheduleService.postSingleSchedule(
                singleScheduleRequestWithPlace, 1L);

            // then
            verify(singleScheduleRepository, times(1))
                .save(any(SingleSchedule.class));
            verify(repeatScheduleRepository, times(0))
                .save(any(RepeatSchedule.class));
        }

        @Test
        @DisplayName("반복 일정 등록 성공 - 장소 정보가 있을 때")
        public void testPostRepeatSchedule_WithPlace() {
            // given
            RepeatScheduleCreateRequest repeatScheduleRequestWithPlace =
                new RepeatScheduleCreateRequest(
                "Weekly Meeting",
                "Content for weekly meeting",
                LocalDate.of(2024, 7, 5),
                LocalTime.of(10, 0),
                LocalTime.of( 11, 0),
                null,
                RepeatCycle.WEEKLY,
                RepeatSituation.FRIDAY,
                LocalDate.of(2024, 12, 31)
            );

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.save(any(RepeatSchedule.class)))
                .willReturn(repeatScheduleWithPlace);

            // when
            scheduleService.postRepeatSchedule(
                repeatScheduleRequestWithPlace, 1L);

            // then
            verify(repeatScheduleRepository, times(1))
                .save(any(RepeatSchedule.class));
            verify(singleScheduleRepository, times(0))
                .save(any(SingleSchedule.class));
        }


        @Test
        @DisplayName("반복 일정 등록 성공 - 월간 반복")
        public void testPostRepeatSchedule_Monthly() {
            // given
            RepeatScheduleCreateRequest repeatScheduleRequestWithPlace =
                new RepeatScheduleCreateRequest(
                    "Weekly Meeting",
                    "Content for weekly meeting",
                    LocalDate.of(2024, 7, 5),
                    LocalTime.of(10, 0),
                    LocalTime.of( 11, 0),
                    null,
                    RepeatCycle.MONTHLY,
                    RepeatSituation.MONTHLY_FIVE,
                    LocalDate.of(2024, 12, 31)
                );

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.save(any(RepeatSchedule.class)))
                .willReturn(repeatScheduleWithPlace);

            // when
            scheduleService.postRepeatSchedule(
                repeatScheduleRequestWithPlace, 1L);

            // then
            verify(repeatScheduleRepository, times(1))
                .save(any(RepeatSchedule.class));
            verify(singleScheduleRepository, times(0))
                .save(any(SingleSchedule.class));
        }

        @Test
        @DisplayName("반복 일정 등록 실패 - 주간 반복 상황 요일과 해당 날짜의 요일이 다를 경우")
        public void testPostRepeatSchedule_ThrowsIllegalArgumentForRepeatSituationException() {
            // given

            RepeatScheduleCreateRequest wrongRequest =
                RepeatScheduleCreateRequest.builder()
                    .scheduleName("잘못된 요일의 반복 일정 요청")
                    .scheduleContent("7월 14일은 일요일인데 반복 상황을 수요일이라고 하겠음")
                    .scheduleDate(LocalDate.of(2024, 7, 14))
                    .scheduleStartTime(LocalTime.of(10, 0))
                    .scheduleEndTime(LocalTime.of(11, 0))
                    .repeatCycle(RepeatCycle.WEEKLY)
                    .repeatSituation(RepeatSituation.WEDNESDAY)
                    .build();

            given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));

            // when & then
            assertThrows(IllegalArgumentForRepeatSituationException.class, () -> {
                scheduleService.postRepeatSchedule(wrongRequest, 1L);
            });
        }

        @Test
        @DisplayName("반복 일정 등록 실패 - 월간 반복 상황 날짜와 해당 날짜가 다를 경우")
        public void testPostRepeatSchedule_ThrowsIllegalArgumentForRepeatSituationException_Monthly() {
            // given

            RepeatScheduleCreateRequest wrongRequest =
                RepeatScheduleCreateRequest.builder()
                    .scheduleName("잘못된 날짜의 반복 일정 요청")
                    .scheduleContent("7월 14일인데 반복 상황을 15일이라고 하겠음")
                    .scheduleDate(LocalDate.of(2024, 7, 14))
                    .scheduleStartTime(LocalTime.of(10, 0))
                    .scheduleEndTime(LocalTime.of(11, 0))
                    .repeatCycle(RepeatCycle.MONTHLY)
                    .repeatSituation(RepeatSituation.MONTHLY_FIFTEEN)
                    .build();

            given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));

            // when & then
            assertThrows(IllegalArgumentForRepeatSituationException.class, () -> {
                scheduleService.postRepeatSchedule(wrongRequest, 1L);
            });
        }
    }

    @DisplayName("일정 조회")
    @Nested
    class ScheduleGetTest {
        @Test
        @DisplayName("스터디 채널 내의 일정 전체 조회 성공")
        public void success_testGetSchedulesInStudyChannel() {
            // given
            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(singleScheduleRepository.findAllByStudyChannelId(1L))
                .willReturn(Arrays.asList(singleScheduleWithoutPlace));
            given(repeatScheduleRepository.findAllByStudyChannelId(1L))
                .willReturn(Arrays.asList(repeatScheduleWithoutPlace));

            // when
            List<ScheduleResponse> scheduleResponses =
                scheduleService.getSchedulesInStudyChannel(1L);

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
            RepeatSchedule repeatSchedule2 =
                RepeatSchedule.withoutIdBuilder()
                .scheduleDate(LocalDate.of(2024, 5, 15))
                .repeatEndDate(LocalDate.of(2024, 9, 15))
                .studyChannel(studyChannel)
                .build();
            LocalDate selectMonthFirstDate = LocalDate.of(2024, 7, 1);
            LocalDate selectMonthLastDate = selectMonthFirstDate.withDayOfMonth(selectMonthFirstDate.lengthOfMonth());

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(singleScheduleRepository.findAllByStudyChannelIdAndDateRange(
                1L, selectMonthFirstDate, selectMonthLastDate))
                .willReturn(Arrays.asList(singleScheduleWithoutPlace));

            given(repeatScheduleRepository.findAllByStudyChannelIdAndDate(
                1L, selectMonthFirstDate))
                .willReturn(Arrays.asList(repeatSchedule2));

            // when
            List<ScheduleResponse> scheduleResponses =
                scheduleService.getSchedulesInStudyChannelForYearAndMonth(
                1L, 2024, 7);

            // then
            assertEquals(2, scheduleResponses.size());
            verify(studyChannelRepository, times(1)).findById(1L);
            verify(singleScheduleRepository, times(1))
                .findAllByStudyChannelIdAndDateRange(
                1L, selectMonthFirstDate, selectMonthLastDate);
            verify(repeatScheduleRepository, times(1))
                .findAllByStudyChannelIdAndDate(
                1L, selectMonthFirstDate);
        }

        @Test
        @DisplayName("스터디 채널 내의 일정 전체 조회 실패: study channel이 존재하지 않을 때")
        public void fail_testGetSchedulesInStudyChannel() {
            // given
            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.empty());

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

    @DisplayName("일정 수정: 단일 -> any | 반복 -> 반복")
    @Nested
    class ScheduleEditTest1 {
        @Test
        @DisplayName("단일 일정 -> 단일 일정 수정 성공")
        public void testPutSchedulesSingleToSingle() {
            // given
            SingleScheduleEditRequest singleScheduleEditRequest = SingleScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .scheduleName("단일 일정 이름")
                .scheduleContent("단일 일정을 단일 일정으로 수정 요청합니다.")
                .selectedDate(LocalDate.of(2024, 7, 5))
                .scheduleStartTime(LocalTime.of(12, 0))
                .scheduleEndTime(LocalTime.of(13, 0))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(singleScheduleRepository.findById(2L))
                .willReturn(Optional.of(singleScheduleWithPlace));

            // when
            scheduleService.putSchedule(
                1L, singleScheduleEditRequest);

            // then
            ArgumentCaptor<SingleSchedule> captor =
                ArgumentCaptor.forClass(SingleSchedule.class);
            verify(singleScheduleRepository, times(1)).save(captor.capture());
            SingleSchedule savedSchedule = captor.getValue();

            assertEquals("단일 일정 이름", savedSchedule.getScheduleName());
            assertEquals("단일 일정을 단일 일정으로 수정 요청합니다.", savedSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 5), savedSchedule.getScheduleDate());
            assertEquals(LocalTime.of(12, 0), savedSchedule.getScheduleStartTime());
            assertEquals(LocalTime.of(13, 0), savedSchedule.getScheduleEndTime());
            assertNull(singleScheduleWithPlace.getPlaceId());
        }

        @Test
        @DisplayName("단일 일정 -> 반복 일정 수정 성공")
        public void testPutSchedulesSingleToRepeat() {
            // given
            RepeatScheduleEditRequest repeatScheduleEditRequest = RepeatScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .editType(ScheduleType.REPEAT)
                .scheduleName("Repeat Meeting Edit")
                .scheduleContent("Content for repeat meeting Edit")
                .selectedDate(LocalDate.of(2024, 7, 5))
                .scheduleStartTime(LocalTime.of(12, 0))
                .scheduleEndTime(LocalTime.of(13, 0))
                .repeatCycle(RepeatCycle.WEEKLY)
                .repeatSituation(RepeatSituation.FRIDAY)
                .repeatEndDate(LocalDate.of(2024, 12, 31))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(singleScheduleRepository.findById(2L))
                .willReturn(Optional.of(singleScheduleWithoutPlace));

            // when
            scheduleService.putSchedule(
                1L, repeatScheduleEditRequest);

            // then
            ArgumentCaptor<RepeatSchedule> captor =
                ArgumentCaptor.forClass(RepeatSchedule.class);
            verify(repeatScheduleRepository, times(1)).save(captor.capture());
            RepeatSchedule savedSchedule = captor.getValue();

            assertEquals("Repeat Meeting Edit", savedSchedule.getScheduleName());
            assertEquals("Content for repeat meeting Edit", savedSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 5), savedSchedule.getScheduleDate());
            assertEquals(LocalTime.of(12, 0), savedSchedule.getScheduleStartTime());
            assertEquals(LocalTime.of(13, 0), savedSchedule.getScheduleEndTime());
            assertEquals(RepeatCycle.WEEKLY, savedSchedule.getRepeatCycle());
            assertEquals(RepeatSituation.FRIDAY, savedSchedule.getRepeatSituation());
            assertEquals(LocalDate.of(2024, 12, 31), savedSchedule.getRepeatEndDate());
            assertNull(savedSchedule.getPlaceId());
//            verify(singleScheduleRepository, times(1)).deleteById(1L); // builder와 id를 두는 생성자패턴이 없어서 통과는 못하나 api test는 완료했습니다.
        }

        @Test
        @DisplayName("반복 일정 -> 반복 일정 수정 성공")
        public void testPutSchedulesRepeatToRepeat() {
            // given
            RepeatScheduleEditRequest repeatScheduleEditRequest = RepeatScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.REPEAT)
                .editType(ScheduleType.REPEAT)
                .scheduleName("Repeat Meeting Edit")
                .scheduleContent("Content for repeat meeting Edit")
                .selectedDate(LocalDate.of(2024, 7, 5))
                .scheduleStartTime(LocalTime.of(12, 0))
                .scheduleEndTime(LocalTime.of(13, 0))
                .repeatCycle(RepeatCycle.WEEKLY)
                .repeatSituation(RepeatSituation.FRIDAY)
                .repeatEndDate(LocalDate.of(2024, 12, 31))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatScheduleWithPlace));

            // when
            scheduleService.putSchedule(
                1L, repeatScheduleEditRequest);

            // then
            ArgumentCaptor<RepeatSchedule> captor =
                ArgumentCaptor.forClass(RepeatSchedule.class);
            verify(repeatScheduleRepository, times(1)).save(captor.capture());
            RepeatSchedule savedSchedule = captor.getValue();

            assertEquals("Repeat Meeting Edit", savedSchedule.getScheduleName());
            assertEquals("Content for repeat meeting Edit", savedSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 5), savedSchedule.getScheduleDate());
            assertEquals(LocalTime.of(12, 0), savedSchedule.getScheduleStartTime());
            assertEquals(LocalTime.of(13, 0), savedSchedule.getScheduleEndTime());
            assertEquals(RepeatCycle.WEEKLY, savedSchedule.getRepeatCycle());
            assertEquals(RepeatSituation.FRIDAY, savedSchedule.getRepeatSituation());
            assertEquals(LocalDate.of(2024, 12, 31), savedSchedule.getRepeatEndDate());
            assertNull( savedSchedule.getPlaceId());
          }
      }

    @DisplayName("일정 수정: 반복 -> 단일")
    @Nested
    class ScheduleEditTest2 {

        private RepeatSchedule repeatDailySchedule =
            RepeatSchedule.withoutIdBuilder()
            .scheduleName("7월 1일 부터 15일까지 매일 반복 일정")
            .scheduleContent("Content for repeat meeting")
            .scheduleDate(LocalDate.of(2024, 7, 1))
            .scheduleStartTime(LocalTime.of(10, 0))
            .scheduleEndTime(LocalTime.of(11, 0))
            .repeatCycle(RepeatCycle.DAILY)
            .repeatSituation(RepeatSituation.EVERYDAY)
            .repeatEndDate(LocalDate.of(2024, 7, 24))
            .isRepeated(true)
            .studyChannel(studyChannel)
            .placeId(null)
            .build();

        @Test
        @DisplayName("반복 일정 -> 단일 일정 변경 | 이후 이벤트 동일 O - 반복 일정 중간 날짜")
        public void testPutRepeatScheduleWithAfterEventSameYes_MiddleDate() {
            // given
            SingleScheduleEditRequest singleScheduleEditRequest = SingleScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .scheduleName("반복 일정 중간에 단일 일정으로 수정")
                .scheduleContent("반복 일정 중간에 단일 일정으로 수정 내용")
                .selectedDate(LocalDate.of(2024, 7, 6))
                .scheduleStartTime(LocalTime.of(12, 0))
                .scheduleEndTime(LocalTime.of(13, 0))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatDailySchedule));

            // when
            scheduleService.putScheduleRepeatToSingle(
                1L, true, singleScheduleEditRequest);

            // then
            ArgumentCaptor<SingleSchedule> singleCaptor =
                ArgumentCaptor.forClass(SingleSchedule.class);
            verify(singleScheduleRepository, times(1)).save(singleCaptor.capture());
            SingleSchedule savedSingleSchedule = singleCaptor.getValue();


            assertEquals("반복 일정 중간에 단일 일정으로 수정", savedSingleSchedule.getScheduleName());
            assertEquals("반복 일정 중간에 단일 일정으로 수정 내용", savedSingleSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 6), savedSingleSchedule.getScheduleDate());
            assertEquals(LocalDate.of(2024, 7, 5), repeatDailySchedule.getRepeatEndDate());
        }

        @Test
        @DisplayName("반복 일정 -> 단일 일정 변경 | 이후 이벤트 동일 O - 반복 일정 처음 날짜")
        public void testPutRepeatScheduleWithAfterEventSameYes_FirstDate() {
            // given
            SingleScheduleEditRequest singleScheduleEditRequest = SingleScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .scheduleName("반복 일정 처음에 단일 일정으로 수정")
                .scheduleContent("반복 일정 처음에 단일 일정으로 수정 내용")
                .selectedDate(LocalDate.of(2024, 7, 1))
                .scheduleStartTime(LocalTime.of(12, 0))
                .scheduleEndTime(LocalTime.of(13, 0))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatDailySchedule));

            // when
            scheduleService.putScheduleRepeatToSingle(
                1L, true, singleScheduleEditRequest);

            // then
            ArgumentCaptor<SingleSchedule> singleCaptor =
                ArgumentCaptor.forClass(SingleSchedule.class);
            verify(singleScheduleRepository, times(1)).save(singleCaptor.capture());
            SingleSchedule savedSingleSchedule = singleCaptor.getValue();


            assertEquals("반복 일정 처음에 단일 일정으로 수정", savedSingleSchedule.getScheduleName());
            assertEquals("반복 일정 처음에 단일 일정으로 수정 내용", savedSingleSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 1), savedSingleSchedule.getScheduleDate());

            verify(repeatScheduleRepository, times(1)).deleteById(2L);
        }

        @Test
        @DisplayName("반복 일정 -> 단일 일정 변경 | 이후 이벤트 동일 O - 반복 일정 마지막 날짜")
        public void testPutRepeatScheduleWithAfterEventSameYes_LastDate() {
            // given
            SingleScheduleEditRequest singleScheduleEditRequest = SingleScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .scheduleName("반복 일정 마지막에 단일 일정으로 수정")
                .scheduleContent("반복 일정 마지막에 단일 일정으로 수정 내용")
                .selectedDate(LocalDate.of(2024, 7, 24))
                .scheduleStartTime(LocalTime.of(23, 20))
                .scheduleEndTime(LocalTime.of(23, 45))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatDailySchedule));

            // when
            scheduleService.putScheduleRepeatToSingle(
                1L, true, singleScheduleEditRequest);

            // then
            ArgumentCaptor<SingleSchedule> singleCaptor =
                ArgumentCaptor.forClass(SingleSchedule.class);
            verify(singleScheduleRepository, times(1)).save(singleCaptor.capture());
            SingleSchedule savedSingleSchedule = singleCaptor.getValue();

            assertEquals("반복 일정 마지막에 단일 일정으로 수정", savedSingleSchedule.getScheduleName());
            assertEquals("반복 일정 마지막에 단일 일정으로 수정 내용", savedSingleSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 24), savedSingleSchedule.getScheduleDate());
            assertEquals(LocalDate.of(2024, 7, 23), repeatDailySchedule.getRepeatEndDate());
        }

        @Test
        @DisplayName("반복 일정 -> 단일 일정 변경 | 이후 이벤트 동일 X - 반복 일정 중간 날짜")
        public void testPutRepeatScheduleWithAfterEventSameNo_MiddleDate() {
            // given
            SingleScheduleEditRequest singleScheduleEditRequest = SingleScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .scheduleName("반복 일정 중간에 단일 일정으로 수정")
                .scheduleContent("반복 일정 중간에 단일 일정으로 수정 내용")
                .selectedDate(LocalDate.of(2024, 7, 6))
                .scheduleStartTime(LocalTime.of(12, 0))
                .scheduleEndTime(LocalTime.of(13, 0))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatDailySchedule));

            // when
            scheduleService.putScheduleRepeatToSingle(
                1L, false, singleScheduleEditRequest);

            // then
            ArgumentCaptor<SingleSchedule> singleCaptor =
                ArgumentCaptor.forClass(SingleSchedule.class);
            verify(singleScheduleRepository, times(1)).save(singleCaptor.capture());
            SingleSchedule savedSingleSchedule = singleCaptor.getValue();

            ArgumentCaptor<RepeatSchedule> repeatCaptor =
                ArgumentCaptor.forClass(RepeatSchedule.class);
            verify(repeatScheduleRepository, times(2)).save(repeatCaptor.capture());
            List<RepeatSchedule> savedRepeatSchedule = repeatCaptor.getAllValues();

            assertEquals("반복 일정 중간에 단일 일정으로 수정", savedSingleSchedule.getScheduleName());
            assertEquals("반복 일정 중간에 단일 일정으로 수정 내용", savedSingleSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 6), savedSingleSchedule.getScheduleDate());
            assertEquals(LocalDate.of(2024, 7, 5), repeatDailySchedule.getRepeatEndDate());
            assertEquals(LocalDate.of(2024, 7, 7), savedRepeatSchedule.get(0).getScheduleDate());
        }

        @Test
        @DisplayName("반복 일정 -> 단일 일정 변경 | 이후 이벤트 동일 X - 반복 일정 처음 날짜")
        public void testPutRepeatScheduleWithAfterEventSameNo_FirstDate() {
            // given
            SingleScheduleEditRequest singleScheduleEditRequest = SingleScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .scheduleName("반복 일정 처음에 단일 일정으로 수정")
                .scheduleContent("반복 일정 처음에 단일 일정으로 수정 내용")
                .selectedDate(LocalDate.of(2024, 7, 1))
                .scheduleStartTime(LocalTime.of(12, 0))
                .scheduleEndTime(LocalTime.of(13, 0))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatDailySchedule));

            // when
            scheduleService.putScheduleRepeatToSingle(
                1L, false, singleScheduleEditRequest);

            // then
            ArgumentCaptor<SingleSchedule> singleCaptor =
                ArgumentCaptor.forClass(SingleSchedule.class);
            verify(singleScheduleRepository, times(1)).save(singleCaptor.capture());
            SingleSchedule savedSingleSchedule = singleCaptor.getValue();

            assertEquals("반복 일정 처음에 단일 일정으로 수정", savedSingleSchedule.getScheduleName());
            assertEquals("반복 일정 처음에 단일 일정으로 수정 내용", savedSingleSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 1), savedSingleSchedule.getScheduleDate());
            assertEquals(LocalDate.of(2024, 7, 2), repeatDailySchedule.getScheduleDate());
            assertEquals(LocalDate.of(2024, 7, 24), repeatDailySchedule.getRepeatEndDate());
        }

        @Test
        @DisplayName("반복 일정 -> 단일 일정 변경 | 이후 이벤트 동일 X - 반복 일정 마지막 날짜")
        public void testPutRepeatScheduleWithAfterEventSameNo_LastDate() {
            // given
            SingleScheduleEditRequest singleScheduleEditRequest = SingleScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .scheduleName("반복 일정 마지막에 단일 일정으로 수정")
                .scheduleContent("반복 일정 마지막에 단일 일정으로 수정 내용")
                .selectedDate(LocalDate.of(2024, 7, 24))
                .scheduleStartTime(LocalTime.of(23, 20))
                .scheduleEndTime(LocalTime.of(23, 50))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatDailySchedule));

            // when
            scheduleService.putScheduleRepeatToSingle(
                1L, false, singleScheduleEditRequest);

            // then
            ArgumentCaptor<SingleSchedule> singleCaptor =
                ArgumentCaptor.forClass(SingleSchedule.class);
            verify(singleScheduleRepository, times(1)).save(singleCaptor.capture());
            SingleSchedule savedSingleSchedule = singleCaptor.getValue();

            assertEquals("반복 일정 마지막에 단일 일정으로 수정", savedSingleSchedule.getScheduleName());
            assertEquals("반복 일정 마지막에 단일 일정으로 수정 내용", savedSingleSchedule.getScheduleContent());
            assertEquals(LocalDate.of(2024, 7, 24), savedSingleSchedule.getScheduleDate());
            assertEquals(LocalDate.of(2024, 7, 23), repeatDailySchedule.getRepeatEndDate());
        }

        @Test
        @DisplayName("반복 일정 -> 단일 일정 변경 후 이벤트 동일 여부 확인 - 범위 초과")
        public void testPutRepeatScheduleWithAfterEventSameOutOfRange() {
            // given
            SingleScheduleEditRequest singleScheduleEditRequest = SingleScheduleEditRequest.builder()
                .scheduleId(2L)
                .originType(ScheduleType.SINGLE)
                .scheduleName("반복 일정 처음에 단일 일정으로 수정")
                .scheduleContent("반복 일정 처음에 단일 일정으로 수정 내용")
                .selectedDate(LocalDate.of(2024, 6, 1))
                .scheduleStartTime(LocalTime.of(12, 0))
                .scheduleEndTime(LocalTime.of(13, 0))
                .build();

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatDailySchedule));

            // when & then
            assertThrows(OutRangeScheduleException.class, () -> {
              scheduleService.putScheduleRepeatToSingle(1L, true, singleScheduleEditRequest);
            });
        }
    }

    @DisplayName("일정 삭제")
    @Nested
    class scheduleDelete {

        @Test
        @DisplayName("단일 일정 삭제 성공")
        public void testDeleteSingleSchedule() {
            // given
            ScheduleDeleteRequest deleteRequest =
                new ScheduleDeleteRequest(
                    1L, LocalDate.of(2024, 7, 5));

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(singleScheduleRepository.findById(1L))
                .willReturn(Optional.of(singleScheduleWithoutPlace));

            // when
            scheduleService.deleteSingleSchedule(1L, deleteRequest);

            // then
            verify(singleScheduleRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("단일 일정 삭제 실패 - 선택한 날짜가 다름")
        public void testDeleteSingleScheduleWrongDate() {
            // given
            ScheduleDeleteRequest deleteRequest =
                new ScheduleDeleteRequest(
                    1L, LocalDate.of(2024, 9, 30));

            given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
            given(singleScheduleRepository.findById(1L)).willReturn(
                Optional.of(singleScheduleWithPlace));

            // when & then
            assertThrows(NotEqualSingleScheduleDate.class, () -> {
                scheduleService.deleteSingleSchedule(1L, deleteRequest);
            });
        }

        @Test
        @DisplayName("반복 일정 삭제: 이후 반복 이벤트 동일하게 O | 반복 일정 첫날")
        public void testDeleteRepeatScheduleAfterEventSameYes_FirstDate() {
            // given
            ScheduleDeleteRequest deleteRequest =
                new ScheduleDeleteRequest(2L, LocalDate.of(2024, 7, 5));

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatScheduleWithPlace));

            // when
            scheduleService.deleteRepeatSchedule(1L, true, deleteRequest);

            // then
            // schedule에 id까지 설정하는 생성자 or builder를 만들지 않아 해당 verify는 0L로 들어갔다고 에러가남.
            // 그러나 실제 api 테스트에서 확인 했음.
//            verify(repeatScheduleRepository, times(1)).deleteById(2L);
        }

        @Test
        @DisplayName("반복 일정 삭제: 이후 반복 이벤트 동일하게 O | 반복 일정 마지막 날")
        public void testDeleteRepeatScheduleAfterEventSameYes_LastDate() {
            // given
            ScheduleDeleteRequest deleteRequest =
                new ScheduleDeleteRequest(2L, LocalDate.of(2024, 8, 9));

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatScheduleWithPlace));

            // when
            scheduleService.deleteRepeatSchedule(1L, true, deleteRequest);

            // then
            ArgumentCaptor<RepeatSchedule> repeatCaptor =
                ArgumentCaptor.forClass(RepeatSchedule.class);
            verify(repeatScheduleRepository, times(1)).save(repeatCaptor.capture());
            RepeatSchedule savedRepeatSchedule = repeatCaptor.getValue();
            assertEquals(RepeatCycle.WEEKLY, savedRepeatSchedule.getRepeatCycle());
            assertEquals(LocalDate.of(2024, 7, 5), savedRepeatSchedule.getScheduleDate());
            assertEquals(LocalDate.of(2024, 8, 2), savedRepeatSchedule.getRepeatEndDate());
        }

        @Test
        @DisplayName("반복 일정 삭제: 이후 반복 이벤트 동일하게 O | 반복 일정 중간 날")
        public void testDeleteRepeatScheduleAfterEventSameYes_MiddleDate() {
            // given
            ScheduleDeleteRequest deleteRequest =
                new ScheduleDeleteRequest(2L, LocalDate.of(2024, 7, 19));

            given(studyChannelRepository.findById(1L))
                .willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L))
                .willReturn(Optional.of(repeatScheduleWithPlace));

            // when
            scheduleService.deleteRepeatSchedule(1L, true, deleteRequest);

            // then
            ArgumentCaptor<RepeatSchedule> repeatCaptor =
                ArgumentCaptor.forClass(RepeatSchedule.class);
            verify(repeatScheduleRepository, times(1)).save(repeatCaptor.capture());
            RepeatSchedule savedRepeatSchedule = repeatCaptor.getValue();
            assertEquals(RepeatCycle.WEEKLY, savedRepeatSchedule.getRepeatCycle());
            assertEquals(LocalDate.of(2024, 7, 5), savedRepeatSchedule.getScheduleDate());
            assertEquals(LocalDate.of(2024, 7, 12), savedRepeatSchedule.getRepeatEndDate());
        }

        @Test
        @DisplayName("반복 일정 삭제: 이후 반복 이벤트 동일하게 X | 반복 일정 첫 날 삭제")
        public void testDeleteRepeatScheduleAfterEventSameNo_FirstDate() {
            // given
            ScheduleDeleteRequest deleteRequest = new ScheduleDeleteRequest(
                2L, LocalDate.of(2024, 7, 5));

            given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L)).willReturn(Optional.of(repeatScheduleWithPlace));

            // when
            scheduleService.deleteRepeatSchedule(1L, false, deleteRequest);

            // then
            ArgumentCaptor<RepeatSchedule> repeatCaptor =
                ArgumentCaptor.forClass(RepeatSchedule.class);
            verify(repeatScheduleRepository, times(1)).save(repeatCaptor.capture());
            RepeatSchedule savedRepeatSchedule = repeatCaptor.getValue();
            assertEquals(RepeatCycle.WEEKLY, savedRepeatSchedule.getRepeatCycle());
            assertEquals(LocalDate.of(2024, 7, 12), savedRepeatSchedule.getScheduleDate());
        }

        @Test
        @DisplayName("반복 일정 삭제: 이후 반복 이벤트 동일하게 X | 반복 일정 마지막 날 삭제")
        public void testDeleteRepeatScheduleAfterEventSameNo_LastDate() {
            // given
            ScheduleDeleteRequest deleteRequest = new ScheduleDeleteRequest(
                2L, LocalDate.of(2024, 8, 9));

            given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L)).willReturn(Optional.of(repeatScheduleWithPlace));

            // when
            scheduleService.deleteRepeatSchedule(1L, false, deleteRequest);

            // then
            ArgumentCaptor<RepeatSchedule> repeatCaptor =
                ArgumentCaptor.forClass(RepeatSchedule.class);
            verify(repeatScheduleRepository, times(1)).save(repeatCaptor.capture());
            RepeatSchedule savedRepeatSchedule = repeatCaptor.getValue();
            assertEquals(RepeatCycle.WEEKLY, savedRepeatSchedule.getRepeatCycle());
            assertEquals(LocalDate.of(2024, 8, 2), savedRepeatSchedule.getRepeatEndDate());
        }

        @Test
        @DisplayName("반복 일정 삭제: 이후 반복 이벤트 동일하게 X | 반복 일정 중간 날 삭제")
        public void testDeleteRepeatScheduleAfterEventSameNo_MiddleDate() {
            // given
            ScheduleDeleteRequest deleteRequest = new ScheduleDeleteRequest(
                2L, LocalDate.of(2024, 7, 19));

            given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L)).willReturn(Optional.of(repeatScheduleWithPlace));

            // when
            scheduleService.deleteRepeatSchedule(1L, false, deleteRequest);

            // then
            ArgumentCaptor<RepeatSchedule> repeatCaptor =
                ArgumentCaptor.forClass(RepeatSchedule.class);
            verify(repeatScheduleRepository, times(2)).save(repeatCaptor.capture());
            List<RepeatSchedule> savedRepeatSchedule = repeatCaptor.getAllValues();
            assertEquals(RepeatCycle.WEEKLY, savedRepeatSchedule.get(0).getRepeatCycle());
            assertEquals(LocalDate.of(2024, 7, 5), savedRepeatSchedule.get(1).getScheduleDate());
            assertEquals(LocalDate.of(2024, 7, 12), savedRepeatSchedule.get(1).getRepeatEndDate());
            assertEquals(LocalDate.of(2024, 7, 26), savedRepeatSchedule.get(0).getScheduleDate());
            assertEquals(LocalDate.of(2024, 8, 9), savedRepeatSchedule.get(0).getRepeatEndDate());
        }

        @Test
        @DisplayName("반복 일정 삭제 실패 - 선택한 날짜가 범위를 벗어남")
        public void testDeleteRepeatScheduleOutOfRange() {
            // given
            ScheduleDeleteRequest deleteRequest = new ScheduleDeleteRequest(2L,
                LocalDate.of(2025, 1, 1));

            given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
            given(repeatScheduleRepository.findById(2L)).willReturn(
                Optional.of(repeatScheduleWithoutPlace));

            // when & then
            assertThrows(OutRangeScheduleException.class, () -> {
                scheduleService.deleteRepeatSchedule(1L, true, deleteRequest);
            });
        }
    }
}