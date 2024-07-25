package com.tenten.studybadge.notification.service;

import com.tenten.studybadge.common.quartz.RepeatScheduleNotificationJob;
import com.tenten.studybadge.common.quartz.SingleScheduleNotificationJob;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleRequestException;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {

    private final Scheduler scheduler;

    // 알림 스케줄링 (단일 일정)
    public void schedulingSingleScheduleNotification(SingleSchedule singleSchedule) {
        LocalDateTime notificationTime = LocalDateTime.of(
            singleSchedule.getScheduleDate(), singleSchedule.getScheduleStartTime()).minusMinutes(10);
        Date notificationDate = Date.from(notificationTime.atZone(ZoneId.systemDefault()).toInstant());

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("scheduleId", singleSchedule.getId());
        jobDataMap.put("scheduleName", singleSchedule.getScheduleName());
        jobDataMap.put("startTime", LocalDateTime.of(
            singleSchedule.getScheduleDate(), singleSchedule.getScheduleStartTime()));
        jobDataMap.put("studyChannelId", singleSchedule.getStudyChannel().getId());

        JobDetail jobDetail = JobBuilder.newJob(SingleScheduleNotificationJob.class)
            .withIdentity("singleScheduleNotificationJob-" + singleSchedule.getId(), "single-schedule-notifications")
            .usingJobData(jobDataMap)
            .build();

        TriggerKey triggerKey = new TriggerKey("singleScheduleNotificationTrigger-" + singleSchedule.getId(), "single-schedule-notifications");

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startAt(notificationDate)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
            .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled single schedule notification: JobKey={}, TriggerKey={}", jobDetail.getKey(), triggerKey);
        } catch (SchedulerException e) {
            log.error("Error scheduling single schedule notification", e);
        }
    }

    // 알림 스케줄링 (반복 일정)
    public void schedulingRepeatScheduleNotification(RepeatSchedule repeatSchedule) {
        LocalDateTime startDateTime = LocalDateTime.of(repeatSchedule.getScheduleDate(), repeatSchedule.getScheduleStartTime()).minusMinutes(10);
        String cronExpression = getCronExpression(repeatSchedule.getRepeatCycle(), startDateTime);

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("scheduleId", repeatSchedule.getId());
        jobDataMap.put("scheduleName", repeatSchedule.getScheduleName());
        jobDataMap.put("startTime", LocalDateTime.of(repeatSchedule.getScheduleDate(), repeatSchedule.getScheduleStartTime()));
        jobDataMap.put("studyChannelId", repeatSchedule.getStudyChannel().getId());

        JobDetail jobDetail = JobBuilder.newJob(RepeatScheduleNotificationJob.class)
            .withIdentity("repeatScheduleNotificationJob-" + repeatSchedule.getId(), "repeat-schedule-notifications")
            .usingJobData(jobDataMap)
            .build();

        TriggerKey triggerKey = new TriggerKey("repeatScheduleNotificationTrigger-" + repeatSchedule.getId(), "repeat-schedule-notifications");

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionFireAndProceed())
            .build();

        try {
            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.addJob(jobDetail, true); // 기존 Job 덮어쓰기
                scheduler.rescheduleJob(triggerKey, trigger); // 트리거 재스케줄링
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
            log.info("Scheduled repeat schedule notification: JobKey={}, TriggerKey={}", jobDetail.getKey(), triggerKey);
        } catch (SchedulerException e) {
            log.error("Error scheduling repeat schedule notification", e);
        }
    }


    // 크론 표현식 생성
    private String getCronExpression(RepeatCycle repeatCycle, LocalDateTime startDateTime) {
        int minute = startDateTime.getMinute();
        int hour = startDateTime.getHour();
        int dayOfMonth = startDateTime.getDayOfMonth();
        int dayOfWeek = startDateTime.getDayOfWeek().getValue();

        return switch (repeatCycle) {
            case DAILY -> String.format("0 %d %d * * ?", minute, hour);
            case WEEKLY -> String.format("0 %d %d ? * %d", minute, hour, dayOfWeek);
            case MONTHLY -> String.format("0 %d %d %d * ?", minute, hour, dayOfMonth);
            default -> throw new IllegalArgumentForScheduleRequestException();
        };
    }

    public void reschedulingSingleScheduleNotification(
        SingleSchedule originSingleSchedule, SingleSchedule newSingleSchedule) {
        unSchedulingSingleScheduleNotification(originSingleSchedule);
        schedulingSingleScheduleNotification(newSingleSchedule);
    }

    public void reSchedulingRepeatScheduleNotification(
        RepeatSchedule originRepeatSchedule, RepeatSchedule newRepeatSchedule) {
        unSchedulingRepeatScheduleNotification(originRepeatSchedule);
        schedulingRepeatScheduleNotification(newRepeatSchedule);
    }

    public void unSchedulingSingleScheduleNotification(SingleSchedule singleSchedule) {
        try {
            JobKey jobKey = new JobKey("singleScheduleNotificationJob-" + singleSchedule.getId(), "single-schedule-notifications");
            TriggerKey triggerKey = new TriggerKey("singleScheduleNotificationTrigger-" + singleSchedule.getId(), "single-schedule-notifications");
            boolean unscheduled = scheduler.unscheduleJob(triggerKey); // 트리거 삭제
            boolean deleted = scheduler.deleteJob(jobKey); // Job 삭제
            log.info("Unscheduled: {}, Deleted: {}", unscheduled, deleted);
            log.info("Successfully unscheduled and deleted single schedule notification job and trigger: JobKey={}, TriggerKey={}", jobKey, triggerKey);
        } catch (SchedulerException e) {
            log.error("Error unscheduling single schedule notification", e);
        }
    }

    public void unSchedulingRepeatScheduleNotification(RepeatSchedule repeatSchedule) {
        try {
            JobKey jobKey = new JobKey("repeatScheduleNotificationJob-" + repeatSchedule.getId(), "repeat-schedule-notifications");
            TriggerKey triggerKey = new TriggerKey("repeatScheduleNotificationTrigger-" + repeatSchedule.getId(), "repeat-schedule-notifications");
            boolean unscheduled = scheduler.unscheduleJob(triggerKey); // 트리거 삭제
            boolean deleted = scheduler.deleteJob(jobKey); // Job 삭제
            log.info("Unscheduled: {}, Deleted: {}", unscheduled, deleted);
            log.info("Successfully unscheduled and deleted repeat schedule notification job and trigger: JobKey={}, TriggerKey={}", jobKey, triggerKey);
        } catch (SchedulerException e) {
            log.error("Error unscheduling repeat schedule notification", e);
        }
    }
}
