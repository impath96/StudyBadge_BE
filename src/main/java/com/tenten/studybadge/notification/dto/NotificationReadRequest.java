package com.tenten.studybadge.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationReadRequest {
    @NotNull(message = "읽음 처리할 알림 id는 필수입니다.")
    private Long notificationId;
}
