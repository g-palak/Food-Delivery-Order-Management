package com.fooddelivery.service;

import com.fooddelivery.dto.response.NotificationResponse;
import java.util.List;

/**
 * Read-model for user notifications.
 *
 * <p>In this assignment, notifications are produced via Spring Application Events.
 * This service exposes only the notification inbox for the authenticated user.</p>
 */
public interface NotificationService {

    /**
     * Return notifications for the current authenticated recipient.
     */
    List<NotificationResponse> getMyNotifications();
}
