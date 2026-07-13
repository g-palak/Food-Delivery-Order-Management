package com.fooddelivery.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotNull(message = "Reviewee ID is required")
        Long revieweeId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        Integer rating,

        @Size(max = 500, message = "Comment cannot exceed 500 characters")
        String comment
) {
}