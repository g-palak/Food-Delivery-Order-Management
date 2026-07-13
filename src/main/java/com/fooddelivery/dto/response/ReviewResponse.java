package com.fooddelivery.dto.response;

import com.fooddelivery.enums.ReviewType;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long orderId,
        Long reviewerId,
        Long revieweeId,
        Integer rating,
        String comment,
        ReviewType reviewType,
        LocalDateTime createdAt
) {
}