package com.tenten.studybadge.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.type.MemberRole;
import com.tenten.studybadge.notification.domain.entitiy.Notification;
import com.tenten.studybadge.notification.domain.repository.NotificationRepository;
import com.tenten.studybadge.type.notification.NotificationType;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notificationScheduleCreate;
    private Notification notificationScheduleDelete;
    private Long memberId;
    @BeforeEach
    public void setup() {
        memberId = 1L;
        notificationScheduleCreate =  Notification.builder()
            .content("일정 생성 알림")
            .url("관련 url")
            .isRead(false)
            .notificationType(NotificationType.SCHEDULE_CREATE)
            .receiver(Member.builder()
                .id(memberId)
                .role(MemberRole.USER)
                .build())
            .build();

        notificationScheduleDelete = Notification.builder()
            .content("일정 삭제 알림")
            .url("관련 url")
            .isRead(false)
            .notificationType(NotificationType.SCHEDULE_DELETE)
            .receiver(Member.builder()
                .id(memberId)
                .role(MemberRole.USER)
                .build())
            .build();
    }

    @Test
    @DisplayName("전체 알림 조회 성공")
    void getNotifications_success() {
        List<Notification> notifications = Arrays.asList(notificationScheduleCreate, notificationScheduleDelete);

        when(notificationRepository.findAllByReceiverId(memberId)).thenReturn(notifications);

        List<Notification> result = notificationService.getNotifications(memberId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(notificationScheduleCreate, result.get(0));
        assertEquals(notificationScheduleDelete, result.get(1));

        verify(notificationRepository, times(1))
            .findAllByReceiverId(memberId);
    }
}