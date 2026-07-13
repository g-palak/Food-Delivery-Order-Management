package com.fooddelivery.repository;

import com.fooddelivery.entity.Notification;
import com.fooddelivery.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientId(Long recipientId);
    List<Notification> findByRecipientIdAndNotificationStatus(Long recipientId, NotificationStatus notificationStatus);
}