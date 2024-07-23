package com.tenten.studybadge.notification.domain;

import com.tenten.studybadge.common.exception.notification.NotificationContentEmptyException;
import com.tenten.studybadge.common.exception.notification.NotificationContentLengthOverException;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class NotificationContent {
    private String content;

    protected NotificationContent() {}

    public NotificationContent(String content) {
        validateContent(content);
        this.content = content;
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new NotificationContentEmptyException();
        }
        if (content.length() > 150) {
            throw new NotificationContentLengthOverException();
        }
    }
}
