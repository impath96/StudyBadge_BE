package com.tenten.studybadge.notification.domain.repository;

import com.tenten.studybadge.notification.domain.entitiy.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiverId(Long receiverId);
    List<Notification> findAllByReceiverIdAndIsReadFalse(Long receiverId);
    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);
}
