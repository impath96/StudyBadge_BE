package com.tenten.studybadge.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenten.studybadge.common.exception.notification.NotificationNotFoundException;
import com.tenten.studybadge.common.redis.RedisPublisher;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.notification.domain.entitiy.Notification;
import com.tenten.studybadge.notification.domain.repository.EmitterRepository;
import com.tenten.studybadge.notification.domain.repository.NotificationRepository;
import com.tenten.studybadge.notification.dto.DummyData;
import com.tenten.studybadge.notification.dto.NotificationReadRequest;
import com.tenten.studybadge.notification.dto.NotificationResponse;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.notification.NotificationType;
import com.tenten.studybadge.type.study.member.StudyMemberStatus;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final long timeout = 60 * 60 * 1000L; // 60분 타임아웃
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;

    public Page<Notification> getNotifications(Long memberId, Pageable pageable) {
        // 특정 사용자의 모든 알림을 조회
        return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(memberId, pageable);
    }

    public void patchNotification(Long memberId, NotificationReadRequest notificationReadRequest) {
        Notification notification = notificationRepository
            .findByIdAndReceiverId(notificationReadRequest.getNotificationId(), memberId)
            .orElseThrow(NotificationNotFoundException::new);

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public Page<Notification> getUnreadNotifications(Long memberId, Pageable pageable) {
        return notificationRepository.findAllByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(memberId, pageable);
    }

    public SseEmitter subscribe(Long memberId, String lastEventId, HttpServletResponse response) {
        String emitterId = makeTimeIncludeId(memberId);
        SseEmitter sseEmitter = new SseEmitter(timeout);

        sseEmitter.onCompletion(() -> {
            log.info("onCompletion 콜백, 이벤트 전송 성공 => 이벤트 삭제 emitterId: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        sseEmitter.onTimeout(() -> {
            log.info("timeOut 콜백, 이벤트 스트림 연결 끊김 => 이벤트 삭제 emitterId: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        sseEmitter.onError((throwable) -> {
            log.error("에러 발생: {}", throwable.getMessage());
            emitterRepository.deleteById(emitterId);
        });

        emitterRepository.save(emitterId, sseEmitter);

        // 503 에러를 방지하기 위한 더미 이벤트 전송
        String eventId = makeTimeIncludeId(memberId);
        sendNotification(sseEmitter, eventId, emitterId,
            new DummyData("알림 서버 연결 성공, EventStream 생성. [memberId=" + memberId + "]"));

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, memberId, emitterId, sseEmitter);
        }

        response.setHeader("X-Accel-Buffering", "no"); // NGINX PROXY 에서의 필요설정 불필요한 버퍼링방지
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Cache-Control", "no-cache");

        return sseEmitter;
    }

    private String makeTimeIncludeId(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId,
        Object data) {
        try {
            String jsonString = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event().id(eventId).data(jsonString));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
            log.error("알림 전송 실패. EmitterId: {}, Exception: {}", emitterId, exception.getMessage());
        }
    }

    private boolean hasLostData(String lastEventId) {
        return lastEventId != null && !lastEventId.isEmpty();
    }

    private void sendLostData(String lastEventId, Long memberId, String emitterId,
        SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByMemberId(String.valueOf(memberId));
        eventCaches.entrySet().stream()
            .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
            .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
        log.info("전송 받지 못한 알림 전송. lastEventId: {}", lastEventId);
    }

    public void sendNotificationToStudyChannel(Long studyChannelId,
        NotificationType notificationType, String content, String url) {

        studyMemberRepository.findAllByStudyChannelIdWithMember(studyChannelId)
            .stream()
            .filter(studyMember -> studyMember.getStudyMemberStatus() == StudyMemberStatus.PARTICIPATING)
            .map(StudyMember::getMember)
            .forEach((member) -> send(member, notificationType, content, url));
    }

    public void send(Member receiver, NotificationType notificationType, String content,
        String url) {
        Notification notification = notificationRepository.save(
            createNotification(receiver, notificationType, content, url));

        String receiverId = String.valueOf(receiver.getId());
        String eventId = receiverId + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(
            receiverId);
        emitters.forEach((key, emitter) -> {
            emitterRepository.saveEventCache(key, notification);
            sendNotification(emitter, eventId, key, notification.toResponse());
        });
        log.info("Notification sent to memberId: {}, notificationType: {}, content: {}, url: {}",
            receiver.getId(), notificationType, content, url);

        // Redis Pub/Sub을 통해 메시지 발행
        try {
            String message = objectMapper.writeValueAsString(notification.toResponse());
            redisPublisher.publish("member-" + receiverId, message);
        } catch (JsonProcessingException e) {
            log.error("메시지 변환 실패: {}", e.getMessage());
        }
    }

    public void processNotification(String memberId, String message) {
        try {
            // 메시지 파싱
            NotificationResponse notificationResponse = objectMapper.readValue(message,
                NotificationResponse.class);

            String eventId = memberId + "_" + System.currentTimeMillis();

            // SSE를 통해 알림 전송
            Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(
                memberId);
            emitters.forEach((key, emitter) -> {
                sendNotification(emitter, eventId, key, notificationResponse);
            });

            log.info("알림 메시지 process: {}", message);
        } catch (JsonProcessingException e) {
            log.error("알림 메시지 process 실패: {}", message, e);
        }
    }

    private Notification createNotification(Member receiver, NotificationType notificationType,
        String content, String url) {
        return Notification.builder()
            .receiver(receiver)
            .notificationType(notificationType)
            .content(content)
            .url(url)
            .isRead(false)
            .build();
    }
}
