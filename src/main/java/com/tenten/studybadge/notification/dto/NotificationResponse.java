package com.tenten.studybadge.notification.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private Long receiverId;
    private String notificationType;
    private String content;
    private String url;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
