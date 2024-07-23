package com.tenten.studybadge.notification.domain.repository;

import com.tenten.studybadge.notification.domain.entitiy.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiverId(Long receiverId);
}
