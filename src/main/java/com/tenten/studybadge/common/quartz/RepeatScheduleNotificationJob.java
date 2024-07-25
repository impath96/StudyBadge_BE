package com.tenten.studybadge.common.quartz;

import static com.tenten.studybadge.common.constant.NotificationConstant.REPEAT_SCHEDULE_URL;
import static com.tenten.studybadge.common.constant.NotificationConstant.TEN_MINUTES_BEFORE_SCHEDULE_START;

import com.tenten.studybadge.notification.service.NotificationService;
import com.tenten.studybadge.type.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepeatScheduleNotificationJob implements Job {

    private final NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long scheduleId = context.getMergedJobDataMap().getLong("scheduleId");
        String scheduleName = context.getMergedJobDataMap().getString("scheduleName");
        LocalDateTime startTime = (LocalDateTime) context.getMergedJobDataMap().get("startTime");
        Long studyChannelId = context.getMergedJobDataMap().getLong("studyChannelId");

        String content = String.format(TEN_MINUTES_BEFORE_SCHEDULE_START, scheduleName);
        String relateUrl = String.format(REPEAT_SCHEDULE_URL, studyChannelId, scheduleId);

        log.info("RepeatScheduleNotificationJob 실행 for scheduleId: {}", scheduleId);
        log.info("스케줄 이름: {}, 시작 시간: {}, 스터디 채널 id: {}", scheduleName, startTime, studyChannelId);

        notificationService.sendNotificationToStudyChannel(
            studyChannelId, NotificationType.SCHEDULE_REMINDER, content, relateUrl);

        log.info("scheduleId: {} 일정 Reminder & 출석 체크하기 10분 전 알림 전송 from RepeatScheduleNotificationJob", scheduleId);
    }
}