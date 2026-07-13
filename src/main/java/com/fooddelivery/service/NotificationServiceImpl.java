package com.fooddelivery.service;

import com.fooddelivery.dto.response.NotificationResponse;
import com.fooddelivery.entity.Notification;
import com.fooddelivery.repository.NotificationRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        Long recipientId = currentUserId();
        return notificationRepository.findByRecipientId(recipientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return Long.parseLong(userDetails.getUsername());
        }
        if (principal instanceof String username) {
            return Long.parseLong(username);
        }
        throw new RuntimeException("Unauthenticated");
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOrder() != null ? notification.getOrder().getId() : null,
                notification.getRecipient() != null ? notification.getRecipient().getId() : null,
                notification.getNotificationType(),
                notification.getNotificationStatus(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}
