package com.tenten.studybadge.notification.domain.repository;

import com.tenten.studybadge.notification.domain.entitiy.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
    Page<Notification> findAllByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
    Optional<Notification> findByIdAndReceiverId(Long id, Long receiverId);

    @Query("SELECT n.id FROM Notification n WHERE n.createdAt < :cutoffDate")
    List<Long> findOldNotificationIds(LocalDateTime cutoffDate, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.id IN :ids")
    void deleteNotificationsByIds(List<Long> ids);
}
