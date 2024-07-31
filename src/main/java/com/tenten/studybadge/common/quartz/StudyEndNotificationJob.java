package com.tenten.studybadge.common.quartz;

import com.tenten.studybadge.notification.service.NotificationService;
import com.tenten.studybadge.type.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class StudyEndNotificationJob implements Job {

    private final NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long studyChannelId = context.getMergedJobDataMap().getLong("studyChannelId");
        String studyChannelName = context.getMergedJobDataMap().getString("studyChannelName");
        String messageTemplate = context.getMergedJobDataMap().getString("messageTemplate");
        String notificationType = context.getMergedJobDataMap().getString("notificationType");

        String content = String.format(messageTemplate, studyChannelName);
        String relateUrl = String.format("/channel/%d/information", studyChannelId); // 클라이언트 url

        notificationService.sendNotificationToStudyChannel(
            studyChannelId, NotificationType.valueOf(notificationType), content, relateUrl);

        log.info("studyChannelId: {}: 스터디 채널에 속한 스터디 멤버들에게 {} 알림 전송 from StudyEndNotificationJob", notificationType, studyChannelId);
    }
}