package com.fooddelivery.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100)
        String name,

        @Size(max = 20)
        String contact
) {
}