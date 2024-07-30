package com.tenten.studybadge.notification.service;

import static com.tenten.studybadge.common.constant.NotificationConstant.STUDY_END_TODAY_AND_REFUND_NOTIFICATION;
import static com.tenten.studybadge.common.constant.NotificationConstant.STUDY_END_TOMORROW_NOTIFICATION;

import com.tenten.studybadge.common.quartz.RepeatScheduleNotificationJob;
import com.tenten.studybadge.common.quartz.SingleScheduleNotificationJob;
import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleRequestException;
import com.tenten.studybadge.common.quartz.StudyEndNotificationJob;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.type.notification.NotificationType;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        LocalDateTime attendanceStartDateTime = LocalDateTime.of(
            singleSchedule.getScheduleDate(), singleSchedule.getScheduleStartTime()).minusMinutes(10);
        Date notificationDate = Date.from(attendanceStartDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // 현재 날짜와 시간과 비교하여 과거 날짜인지 확인
        if (attendanceStartDateTime.isBefore(LocalDateTime.now())) {
            log.info("출석 체크 시작 시간이 과거 날짜이므로 단일 일정 출석 체크 알림 스케줄링을 생략합니다.: " + attendanceStartDateTime);
            return; // 과거 날짜일 경우 스케줄링을 건너뜁니다.
        }

        LocalDate scheduleDate = singleSchedule.getScheduleDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = scheduleDate.format(formatter);

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("scheduleId", singleSchedule.getId());
        jobDataMap.put("scheduleName", singleSchedule.getScheduleName());
        jobDataMap.put("startTime", LocalDateTime.of(
            scheduleDate, singleSchedule.getScheduleStartTime()));
        jobDataMap.put("studyChannelId", singleSchedule.getStudyChannel().getId());
        jobDataMap.put("formattedDate", formattedDate);

        JobDetail jobDetail = JobBuilder.newJob(SingleScheduleNotificationJob.class)
            .withIdentity("singleScheduleNotificationJob-" + singleSchedule.getId(), "single-schedule-notifications")
            .usingJobData(jobDataMap)
            .build();

        TriggerKey triggerKey = new TriggerKey("singleScheduleNotificationTrigger-" + singleSchedule.getId(), "single-schedule-notifications");

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startAt(notificationDate)
            .endAt(notificationDate)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withMisfireHandlingInstructionNextWithExistingCount())
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
        LocalDate scheduleDate = repeatSchedule.getScheduleDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = scheduleDate.format(formatter);
        LocalDateTime startDateTime = LocalDateTime.of(
            repeatSchedule.getScheduleDate(), repeatSchedule.getScheduleStartTime()).minusMinutes(10);
        LocalDateTime endDateTime = repeatSchedule.getRepeatEndDate().atTime(23, 59, 59); // 하루의 끝으로 설정
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // 현재 날짜와 시간과 비교하여 반복 끝나는 날짜가 과거 날짜인지 확인
        if (repeatSchedule.getRepeatEndDate().isBefore(LocalDate.now())) {
            log.info("반복 끝나는 날짜가 현재보다 과거 날짜이므로 반복 일정 출석 체크 알림 스케줄링을 생략합니다.: " + endDateTime);
            return; // 과거 날짜일 경우 스케줄링을 건너뜁니다.
        }

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("scheduleId", repeatSchedule.getId());
        jobDataMap.put("scheduleName", repeatSchedule.getScheduleName());
        jobDataMap.put("startTime", LocalDateTime.of(repeatSchedule.getScheduleDate(), repeatSchedule.getScheduleStartTime()));
        jobDataMap.put("studyChannelId", repeatSchedule.getStudyChannel().getId());
        jobDataMap.put("formattedDate", formattedDate);

        JobDetail jobDetail = JobBuilder.newJob(RepeatScheduleNotificationJob.class)
            .withIdentity("repeatScheduleNotificationJob-" + repeatSchedule.getId(), "repeat-schedule-notifications")
            .usingJobData(jobDataMap)
            .build();

        String cronExpression = getCronExpression(repeatSchedule.getRepeatCycle(), startDateTime);

        TriggerKey triggerKey = new TriggerKey("repeatScheduleNotificationTrigger-" + repeatSchedule.getId(), "repeat-schedule-notifications");
        try {
            Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                    .withMisfireHandlingInstructionDoNothing())
                .startAt(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()))  // Start time 설정
                .endAt(endDate)
                .build();

            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.addJob(jobDetail, true); // 기존 Job 덮어쓰기
                scheduler.rescheduleJob(triggerKey, trigger); // 트리거 재스케줄링
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
            log.info("Scheduled repeat schedule notification: JobKey={}, TriggerKey={}", jobDetail.getKey(), triggerKey);
        } catch (SchedulerException e) {
            log.error("Error scheduling repeat schedule notification", e);
            e.printStackTrace();  // 예외의 상세 정보 출력
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException 예외의 상세 정보 출력
            log.error("Illegal argument encountered while scheduling repeat schedule notification", e);
            System.out.println("IllegalArgumentException: " + e.getMessage());
            System.out.println("StackTrace: ");
            e.printStackTrace(System.out);
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

    // 알림 스케줄링:
    // 1. 스터디 채널 끝나기 하루 전 알림
    // 2. 스터디 채널 끝 + 환급은 다음날 하는 것 알림
    public void scheduleStudyEndNotifications(StudyChannel studyChannel) {

        LocalDate studyEndDate = studyChannel.getStudyDuration().getStudyEndDate();
        // 현재 날짜와 시간과 비교하여 스터디 채널 끝나는 날짜가 과거 날짜인지 확인
        if (studyEndDate.isBefore(LocalDate.now())) {
            log.info("스터디 채널 끝나는 날짜가 현재보다 과거 날짜이므로 스터디 채널 종료 관련 알림 스케줄링을 생략합니다.: " + studyEndDate);
            return; // 과거 날짜일 경우 스케줄링을 건너뜁니다.
        }

//        // 오전 8시에 스터디 종료 관련 알림 일괄 전송
//        LocalDateTime customTime = studyEndDate.atStartOfDay()
//            .withHour(8).withMinute(0).withSecond(0).withNano(0);

        // 알림 api 테스트를 위해 마감 + 10분 뒤에 알림 전송 확인하도록 설정
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute() + 10;
        LocalDateTime customTime = studyEndDate.atStartOfDay()
            .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        scheduleNotification(
            studyChannel, NotificationType.STUDY_END_TOMORROW,
            "study-end-tomorrow-group", STUDY_END_TOMORROW_NOTIFICATION, customTime.minusDays(1));
        scheduleNotification(
            studyChannel, NotificationType.STUDY_END_TODAY,
            "study-end-today-group", STUDY_END_TODAY_AND_REFUND_NOTIFICATION, customTime);
    }

    private void scheduleNotification(StudyChannel studyChannel, NotificationType notificationType, String groupName, String messageTemplate, LocalDateTime notificationTime) {
        Date notificationDate = Date.from(notificationTime.atZone(ZoneId.systemDefault()).toInstant());
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("studyChannelId", studyChannel.getId());
        jobDataMap.put("studyChannelName", studyChannel.getName());
        jobDataMap.put("notificationType", notificationType.name());
        jobDataMap.put("messageTemplate", messageTemplate);

        String jobIdentity = String.format("studyEndNotificationJob-%d-%s", studyChannel.getId(), notificationTime);
        JobKey jobKey = JobKey.jobKey(jobIdentity, groupName);

        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            JobDetail jobDetail = JobBuilder.newJob(StudyEndNotificationJob.class)
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .build();

            TriggerKey triggerKey = new TriggerKey("studyEndNotificationTrigger-" + studyChannel.getId() + "-" + notificationTime, groupName);
            Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(notificationDate)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withMisfireHandlingInstructionFireNow())
                .build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("스터디 채널 id: {} 스터디 멤버들에게 보낼 {} 알림을 스케줄링 했습니다.", notificationType.getDescription(), studyChannel.getId());
            log.info("Scheduled study end notification: JobKey={}, TriggerKey={}", jobDetail.getKey(), trigger.getKey());
        } catch (SchedulerException e) {
            log.error("Error scheduling study end notification", e);
        }
    }
}
