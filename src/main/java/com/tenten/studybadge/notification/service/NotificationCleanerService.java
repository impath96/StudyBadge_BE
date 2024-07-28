package com.tenten.studybadge.notification.service;

import com.tenten.studybadge.notification.domain.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCleanerService {
    private final NotificationRepository notificationRepository;

    private static final int BATCH_SIZE = 2000;

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시 실행
    public void deleteOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        log.info("삭제 되어야할 cutoffDate: {}", cutoffDate);
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        List<Long> oldNotificationIds;

        do {
            oldNotificationIds = notificationRepository.findOldNotificationIds(cutoffDate, pageable);
            if (!oldNotificationIds.isEmpty()) {
                notificationRepository.deleteNotificationsByIds(oldNotificationIds);
                if (oldNotificationIds.size() == BATCH_SIZE) {
                    log.info("Batch size {}개의 30일 지난 알림을 삭제했습니다.", oldNotificationIds.size());
                } else {
                    log.info("Batch size 미만인 {}개의 30일 지난 알림을 삭제했습니다.", oldNotificationIds.size());
                }
            }
        } while (oldNotificationIds.size() == BATCH_SIZE);
    }
}
