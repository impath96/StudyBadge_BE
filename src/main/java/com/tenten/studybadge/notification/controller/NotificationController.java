package com.tenten.studybadge.notification.controller;

import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 API")
public class NotificationController {
    private final NotificationService notificationService;

    // 세션 연결
    @GetMapping(value = "/api/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "세션 연결", description = "클라이언트 측에서 세션 연결하는 api")
    @Parameter(name = "Last-Event-ID", description = "마지막 event id, 필수는 아님" )
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails memberDetails,
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return notificationService.subscribe(memberDetails.getId(), lastEventId);
    }
}
