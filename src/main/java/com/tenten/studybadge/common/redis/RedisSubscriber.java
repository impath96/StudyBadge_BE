package com.tenten.studybadge.common.redis;

import com.tenten.studybadge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final NotificationService notificationService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String messageBody = new String(message.getBody());

        // 채널 이름에서 memberId를 추출하여 해당 클라이언트에게 메시지 전송
        String memberId = channel.split("-")[1];
        log.info("member id: {} 알림 proccess 전송 message {}", memberId, messageBody);
        notificationService.processNotification(memberId, messageBody);
    }
}