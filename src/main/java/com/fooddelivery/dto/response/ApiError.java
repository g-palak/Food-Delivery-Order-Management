package com.fooddelivery.dto.response;

import java.time.LocalDateTime;

/**
 * Uniform API error shape for all client-facing failures.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
