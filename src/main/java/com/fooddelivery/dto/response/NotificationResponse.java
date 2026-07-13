package com.fooddelivery.dto.response;

import com.fooddelivery.enums.NotificationStatus;
import com.fooddelivery.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long orderId,
        Long recipientId,
        NotificationType notificationType,
        NotificationStatus notificationStatus,
        String message,
        LocalDateTime createdAt
) {
}