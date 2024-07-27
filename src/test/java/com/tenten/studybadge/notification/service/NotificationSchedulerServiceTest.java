package com.tenten.studybadge.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.entity.StudyDuration;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

@ExtendWith(MockitoExtension.class)
public class NotificationSchedulerServiceTest {

    @InjectMocks
    private NotificationSchedulerService notificationSchedulerService;

    @Mock
    private Scheduler scheduler;

    @Test
    @DisplayName("오늘 - 내일 일간 반복하는 일정이 모레에는 일정이 안울리는지 테스트")
    public void testSchedulingRepeatScheduleNotification() throws SchedulerException {
        // given
        RepeatSchedule repeatSchedule = mock(RepeatSchedule.class);
        StudyChannel studyChannel = mock(StudyChannel.class);

        when(studyChannel.getId()).thenReturn(1L);
        when(repeatSchedule.getStudyChannel()).thenReturn(studyChannel);
        when(repeatSchedule.getScheduleDate()).thenReturn(LocalDate.now());
        when(repeatSchedule.getScheduleStartTime()).thenReturn(LocalTime.of(12, 0));
        when(repeatSchedule.getRepeatEndDate()).thenReturn(LocalDate.now().plusDays(1)); // 내일
        when(repeatSchedule.getRepeatCycle()).thenReturn(RepeatCycle.DAILY);

        // Trigger 캡처
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);

        // when
        notificationSchedulerService.schedulingRepeatScheduleNotification(repeatSchedule);

        // then
        // 스케줄러가 올바른 트리거로 호출되었는지 검증
        verify(scheduler).scheduleJob(any(JobDetail.class), triggerCaptor.capture());
        Trigger trigger = triggerCaptor.getValue();

        // 종료 날짜가 올바른지 검증
        Date expectedEndDate = Date.from(LocalDate.now().plusDays(1).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        assertEquals(expectedEndDate, trigger.getEndTime());

        // 종료 날짜 이후 트리거가 실행되지 않는지 검증
        Date afterEndDate = Date.from(LocalDate.now().plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date nextFireTime = trigger.getFireTimeAfter(afterEndDate);
        assertNull(nextFireTime, "Trigger는 종료 날짜 이후에 실행되지 않아야 합니다");
    }

    @Test
    @DisplayName("스터디 종료 알림 스케줄링 테스트")
    public void testScheduleStudyEndNotifications() throws SchedulerException {
        // given
        StudyChannel studyChannel = mock(StudyChannel.class);
        StudyDuration studyDuration = mock(StudyDuration.class);

        when(studyChannel.getId()).thenReturn(1L);
        when(studyChannel.getName()).thenReturn("Test Study Channel");
        when(studyChannel.getStudyDuration()).thenReturn(studyDuration);
        when(studyDuration.getStudyEndDate()).thenReturn(LocalDate.now().plusDays(1));

        LocalDateTime customTime = studyDuration.getStudyEndDate().atStartOfDay()
            .withHour(8).withMinute(0).withSecond(0).withNano(0);

        // Trigger 캡처
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);

        // when
        notificationSchedulerService.scheduleStudyEndNotifications(studyChannel);

        // then
        verify(scheduler, times(2)).scheduleJob(any(JobDetail.class), triggerCaptor.capture());
        List<Trigger> triggers = triggerCaptor.getAllValues();

        // 첫 번째 트리거 검증 (종료 하루 전 알림)
        Trigger firstTrigger = triggers.get(0);
        Date expectedFirstNotificationTime = Date.from(customTime.minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        assertEquals(expectedFirstNotificationTime, firstTrigger.getStartTime());

        // 두 번째 트리거 검증 (종료 당일 알림)
        Trigger secondTrigger = triggers.get(1);
        Date expectedSecondNotificationTime = Date.from(customTime.atZone(ZoneId.systemDefault()).toInstant());
        assertEquals(expectedSecondNotificationTime, secondTrigger.getStartTime());

        // 종료 날짜 이후 트리거가 실행되지 않는지 검증
        Date afterEndDate = Date.from(customTime.plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        Date nextFireTime = secondTrigger.getFireTimeAfter(afterEndDate);
        assertNull(nextFireTime, "Trigger는 종료 날짜 이후에 실행되지 않아야 합니다");
    }

//    @Test
//    @DisplayName("그제 - 어제 일간 반복하는 일정이 오늘에 일정이 안울리는지 테스트 : if문 생략했을때")
//    public void testSchedulingRepeatScheduleNotification_deleteIf() throws SchedulerException {
//        // 본 테스트는 NotificaitionSchedulerService.java
//        // schedulingRepeatScheduleNotification메서드에 있는 if문을 생략해야 통과됩니다. > 현재 Line 81: 줄은 바뀔 수도 있음
//        // given
//        RepeatSchedule repeatSchedule = mock(RepeatSchedule.class);
//        StudyChannel studyChannel = mock(StudyChannel.class);
//
//        when(studyChannel.getId()).thenReturn(1L);
//        when(repeatSchedule.getStudyChannel()).thenReturn(studyChannel);
//        when(repeatSchedule.getScheduleDate()).thenReturn(LocalDate.now().minusDays(2));
//        when(repeatSchedule.getScheduleStartTime()).thenReturn(LocalTime.of(12, 0));
//        when(repeatSchedule.getRepeatEndDate()).thenReturn(LocalDate.now().minusDays(1)); // 어제
//        when(repeatSchedule.getRepeatCycle()).thenReturn(RepeatCycle.DAILY);
//
//        // Trigger 캡처
//        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
//
//        // when
//        notificationSchedulerService.schedulingRepeatScheduleNotification(repeatSchedule);
//
//        // then
//        // 스케줄러가 올바른 트리거로 호출되었는지 검증
//        verify(scheduler).scheduleJob(any(JobDetail.class), triggerCaptor.capture());
//        Trigger trigger = triggerCaptor.getValue();
//
//        // 종료 날짜가 올바른지 검증
//        Date expectedEndDate = Date.from(LocalDate.now().minusDays(1).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
//        assertEquals(expectedEndDate, trigger.getEndTime());
//
//        // 종료 날짜 이후 트리거가 실행되지 않는지 검증
//        Date afterEndDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date nextFireTime = trigger.getFireTimeAfter(afterEndDate);
//        assertNull(nextFireTime, "Trigger는 종료 날짜 이후에 실행되지 않아야 합니다");
//    }
}