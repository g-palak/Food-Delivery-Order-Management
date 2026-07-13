package com.fooddelivery.dto.response;

import com.fooddelivery.enums.AssignmentStatus;
import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id,
        Long orderId,
        Long deliveryPartnerId,
        AssignmentStatus assignmentStatus,
        LocalDateTime updatedAt
) {
}