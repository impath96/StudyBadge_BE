package com.tenten.studybadge.notification.service;

import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.notification.domain.entitiy.Notification;
import com.tenten.studybadge.notification.domain.repository.EmitterRepository;
import com.tenten.studybadge.notification.domain.repository.NotificationRepository;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.type.notification.NotificationType;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final long timeout = 60 * 1000L; // 1분 타임아웃
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final StudyMemberRepository studyMemberRepository;

    public SseEmitter subscribe(Long memberId, String lastEventId) {
        String emitterId = makeTimeIncludeId(memberId);
        SseEmitter sseEmitter = emitterRepository.save(emitterId, new SseEmitter(timeout));

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

        // 503 에러를 방지하기 위한 더미 이벤트 전송
        String eventId = makeTimeIncludeId(memberId);
        sendNotification(sseEmitter, eventId, emitterId, "알림 서버 연결 성공, EventStream 생성. [memberId=" + memberId + "]");

        // 하트비트 전송
        sendHeartbeat(sseEmitter, emitterId);

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, memberId, emitterId, sseEmitter);
        }

        return sseEmitter;
    }

    private String makeTimeIncludeId(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event().id(eventId).data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
            log.error("알림 전송 실패. EmitterId: {}, Exception: {}", emitterId, exception.getMessage());
        }
    }

    private boolean hasLostData(String lastEventId) {
        return lastEventId != null && !lastEventId.isEmpty();
    }

    private void sendLostData(String lastEventId, Long memberId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByMemberId(String.valueOf(memberId));
        eventCaches.entrySet().stream()
            .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
            .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
        log.info("전송 받지 못한 알림 전송. lastEventId: {}", lastEventId);
    }

    private void sendHeartbeat(SseEmitter sseEmitter, String emitterId) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                sseEmitter.send(SseEmitter.event()
                    .id("heartbeat")
                    .name("heartbeat")
                    .data("heartbeat"));
                log.info("주기적인 하트비트가 전송되었습니다.");
            } catch (IOException e) {
                log.error("Error sending heartbeat: {}", e.getMessage());
                emitterRepository.deleteById(emitterId);
            }
        }, 0, timeout / 2, TimeUnit.MILLISECONDS); // 30초 간격으로 하트비트 전송
    }

    public void sendNotificationToStudyChannel(Long studyChannelId,
        NotificationType notificationType, String content, String url) {

        List<Member> members = studyMemberRepository.findAllByStudyChannelIdWithMember(studyChannelId)
            .stream()
            .map(StudyMember::getMember)
            .collect(Collectors.toList());

        for (Member member : members) {
            send(member, notificationType, content, url);
        }
    }

    public void send(Member receiver, NotificationType notificationType, String content, String url) {
        Notification notification = notificationRepository.save(
            createNotification(receiver, notificationType, content, url));

        String receiverId = String.valueOf(receiver.getId());
        String eventId = receiverId + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(receiverId);
        emitters.forEach((key, emitter) -> {
            emitterRepository.saveEventCache(key, notification);
            sendNotification(emitter, eventId, key, notification.toResponse());
        });
        log.info("Notification sent to memberId: {}, notificationType: {}, content: {}, url: {}", receiver.getId(), notificationType, content, url);
    }

    private Notification createNotification(Member receiver, NotificationType notificationType, String content, String url) {
        return Notification.builder()
            .receiver(receiver)
            .notificationType(notificationType)
            .content(content)
            .url(url)
            .isRead(false)
            .build();
    }
}
