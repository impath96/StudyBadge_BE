package com.tenten.studybadge.schedule.service;

import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForRepeatScheduleEditRequestException;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForRepeatSituationException;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleRequestException;
import com.tenten.studybadge.common.exception.schedule.NotEqualSingleScheduleDate;
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
import com.tenten.studybadge.schedule.dto.ScheduleDeleteRequest;
import com.tenten.studybadge.schedule.dto.ScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleResponse;
import com.tenten.studybadge.schedule.dto.SingleScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.SingleScheduleEditRequest;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.type.schedule.ScheduleOriginType;
import java.time.DayOfWeek;
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

    public void postSingleSchedule(SingleScheduleCreateRequest singleScheduleCreateRequest, Long studyChannelId) {
        StudyChannel studyChannel =  studyChannelRepository.findById(studyChannelId)
            .orElseThrow(NotFoundStudyChannelException::new);

        singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
                .scheduleName(singleScheduleCreateRequest.getScheduleName())
                .scheduleContent(singleScheduleCreateRequest.getScheduleContent())
                .scheduleDate(singleScheduleCreateRequest.getScheduleDate())
                .scheduleStartTime(singleScheduleCreateRequest.getScheduleStartTime())
                .scheduleEndTime(singleScheduleCreateRequest.getScheduleEndTime())
                .isRepeated(false)
                .studyChannel(studyChannel)
                .placeId(singleScheduleCreateRequest.getPlaceId())
                .build());

    }

    public void postRepeatSchedule(RepeatScheduleCreateRequest repeatScheduleCreateRequest, Long studyChannelId) {
        StudyChannel studyChannel =  studyChannelRepository.findById(studyChannelId)
            .orElseThrow(NotFoundStudyChannelException::new);

        RepeatCycle repeatCycle = repeatScheduleCreateRequest.getRepeatCycle();
        LocalDate scheduleDate = repeatScheduleCreateRequest.getScheduleDate();
        RepeatSituation repeatSituation = repeatScheduleCreateRequest.getRepeatSituation();
        switch (repeatCycle) {
            case DAILY -> {
                // DAILY 주기에서는 특별한 검증이 필요하지 않으므로 통과
            }
            case WEEKLY -> {
                if (!repeatSituation.equals(scheduleDate.getDayOfWeek())) {
                    throw new IllegalArgumentForRepeatSituationException();
                }
            }
            case MONTHLY -> {
                if (!repeatSituation.equals(scheduleDate.getDayOfMonth())) {
                    throw new IllegalArgumentForRepeatSituationException();
                }
            }
            default -> throw new IllegalArgumentForScheduleRequestException();
        }

        repeatScheduleRepository.save(RepeatSchedule.withoutIdBuilder()
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
            .build());
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
            repeatScheduleRepository.deleteById(singleScheduleEditRequest.getScheduleId());
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
        } else {
            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        }

        // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
        if (repeatSchedule.getScheduleDate().equals(repeatSchedule.getRepeatEndDate())) {
            singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
                .scheduleName(repeatSchedule.getScheduleName())
                .scheduleContent(repeatSchedule.getScheduleContent())
                .scheduleDate(repeatSchedule.getScheduleDate())
                .scheduleStartTime(repeatSchedule.getScheduleStartTime())
                .scheduleEndTime(repeatSchedule.getScheduleEndTime())
                .isRepeated(false)
                .studyChannel(repeatSchedule.getStudyChannel())
                .placeId(repeatSchedule.getPlaceId())
                .build());
            repeatScheduleRepository.deleteById(singleScheduleEditRequest.getScheduleId());
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
            repeatScheduleRepository.save(secondRepeatSchedule);

            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        }

      // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
      if (repeatSchedule.getScheduleDate().equals(repeatSchedule.getRepeatEndDate())) {
        singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
            .scheduleName(repeatSchedule.getScheduleName())
            .scheduleContent(repeatSchedule.getScheduleContent())
            .scheduleDate(repeatSchedule.getScheduleDate())
            .scheduleStartTime(repeatSchedule.getScheduleStartTime())
            .scheduleEndTime(repeatSchedule.getScheduleEndTime())
            .isRepeated(false)
            .studyChannel(repeatSchedule.getStudyChannel())
            .placeId(repeatSchedule.getPlaceId())
            .build());
        repeatScheduleRepository.deleteById(singleScheduleEditRequest.getScheduleId());
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

    public void deleteSingleSchedule(Long studyChannelId, ScheduleDeleteRequest scheduleDeleteRequest) {
        StudyChannel studyChannel = studyChannelRepository.findById(studyChannelId)
            .orElseThrow(NotFoundStudyChannelException::new);

        SingleSchedule singleSchedule = singleScheduleRepository.findById(
                scheduleDeleteRequest.getScheduleId())
            .orElseThrow(NotFoundSingleScheduleException::new);

        if (!scheduleDeleteRequest.getSelectedDate().equals(singleSchedule.getScheduleDate())) {
            throw new NotEqualSingleScheduleDate();
        }

        singleScheduleRepository.deleteById(scheduleDeleteRequest.getScheduleId());
    }

    public void deleteRepeatSchedule(Long studyChannelId, Boolean isAfterEventSame, ScheduleDeleteRequest scheduleDeleteRequest) {
        StudyChannel studyChannel = studyChannelRepository.findById(studyChannelId)
            .orElseThrow(NotFoundStudyChannelException::new);

        RepeatSchedule repeatSchedule = repeatScheduleRepository.findById(
                scheduleDeleteRequest.getScheduleId())
            .orElseThrow(NotFoundRepeatScheduleException::new);

        LocalDate selectedDate = scheduleDeleteRequest.getSelectedDate();

        if (isNotIncluded(selectedDate, repeatSchedule.getScheduleDate(), repeatSchedule.getRepeatEndDate())) {
            throw new OutRangeScheduleException();
        }

        if (isAfterEventSame) {
            deleteRepeatScheduleAfterEventSameYes(selectedDate, repeatSchedule);
        } else if (!isAfterEventSame) {
            deleteRepeatScheduleAfterEventSameNo(selectedDate, repeatSchedule);
        }
    }

    public void deleteRepeatScheduleAfterEventSameYes(LocalDate selectedDate, RepeatSchedule repeatSchedule) {

        if (selectedDate.equals(repeatSchedule.getScheduleDate())) {
            // 선택 날짜 repeat schedule 삭제
            repeatScheduleRepository.deleteById(repeatSchedule.getId());
        } else if (isNextRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule.getScheduleDate())) {
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
            repeatScheduleRepository.deleteById(repeatSchedule.getId());
        } else {
            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        }
        // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
        if (repeatSchedule.getScheduleDate().equals(repeatSchedule.getRepeatEndDate())) {
            singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
                .scheduleName(repeatSchedule.getScheduleName())
                .scheduleContent(repeatSchedule.getScheduleContent())
                .scheduleDate(repeatSchedule.getScheduleDate())
                .scheduleStartTime(repeatSchedule.getScheduleStartTime())
                .scheduleEndTime(repeatSchedule.getScheduleEndTime())
                .isRepeated(false)
                .studyChannel(repeatSchedule.getStudyChannel())
                .placeId(repeatSchedule.getPlaceId())
                .build());
            repeatScheduleRepository.deleteById(repeatSchedule.getId());
        }
    }

    public void deleteRepeatScheduleAfterEventSameNo(LocalDate selectedDate, RepeatSchedule repeatSchedule) {
        if (selectedDate.equals(repeatSchedule.getScheduleDate())) {
            // 기존 반복 일정: scheduleDate = scheduleDate + (주기 1)으로 변경
            changeRepeatStartDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        } else if (selectedDate.equals(repeatSchedule.getRepeatEndDate())) {
            // 기존 반복 일정: endDate = endDate - (주기 1)으로 변경
            changeRepeatEndDate(selectedDate,repeatSchedule.getRepeatCycle(), repeatSchedule);
        } else if (isNextRepeatStartDate(selectedDate,
            repeatSchedule.getRepeatCycle(), repeatSchedule.getScheduleDate())) {

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

        } else if (isFrontRepeatEndDate(selectedDate,
            repeatSchedule.getRepeatCycle(), repeatSchedule.getRepeatEndDate())) {

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
            RepeatSchedule secondRepeatSchedule = makeAfterCycleRepeatSchedule(selectedDate,  repeatSchedule);
            repeatScheduleRepository.save(secondRepeatSchedule);

            changeRepeatEndDate(selectedDate, repeatSchedule.getRepeatCycle(), repeatSchedule);
        }

        // 만일 변경한 기존 반복 일정이 반복 시작 날짜와 끝나는 날짜가 같을 경우 단일 일정으로 변경한다.
        if (repeatSchedule.getScheduleDate().equals(repeatSchedule.getRepeatEndDate())) {
            singleScheduleRepository.save(SingleSchedule.withoutIdBuilder()
                .scheduleName(repeatSchedule.getScheduleName())
                .scheduleContent(repeatSchedule.getScheduleContent())
                .scheduleDate(repeatSchedule.getScheduleDate())
                .scheduleStartTime(repeatSchedule.getScheduleStartTime())
                .scheduleEndTime(repeatSchedule.getScheduleEndTime())
                .isRepeated(false)
                .studyChannel(repeatSchedule.getStudyChannel())
                .placeId(repeatSchedule.getPlaceId())
                .build());
            repeatScheduleRepository.deleteById(repeatSchedule.getId());
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
}
