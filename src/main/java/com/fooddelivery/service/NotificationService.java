package com.fooddelivery.service;

import com.fooddelivery.dto.response.NotificationResponse;
import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications();
}