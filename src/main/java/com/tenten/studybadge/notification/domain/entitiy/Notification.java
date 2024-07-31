package com.tenten.studybadge.notification.domain.entitiy;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.notification.domain.NotificationContent;
import com.tenten.studybadge.notification.domain.RelatedUrl;
import com.tenten.studybadge.notification.dto.NotificationResponse;
import com.tenten.studybadge.type.notification.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@ToString
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_created_at", columnList = "createdAt"))
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Embedded
    private NotificationContent content;

    @Embedded
    private RelatedUrl url;

    @Setter
    @Column(nullable = false)
    private Boolean isRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50) // 충분한 길이로 설정
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member receiver;

    @Builder
    public Notification(Member receiver, NotificationType notificationType, String content, String url, Boolean isRead) {
        this.receiver = receiver;
        this.notificationType = notificationType;
        this.content = new NotificationContent(content);
        this.url = new RelatedUrl(url);
        this.isRead = isRead;
    }

    public String getContent() {
      return content.getContent();
    }

    public String getUrl() {
      return url.getUrl();
    }

    public NotificationResponse toResponse() {
        return NotificationResponse.builder()
            .notificationId(this.id)
            .receiverId(this.receiver.getId())
            .notificationType(this.notificationType.getDescription())
            .content(this.getContent())
            .url(this.getUrl())
            .isRead(this.isRead)
            .createdAt(this.getCreatedAt())
            .build();
    }
}
