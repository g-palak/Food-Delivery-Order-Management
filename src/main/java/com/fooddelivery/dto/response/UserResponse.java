package com.fooddelivery.dto.response;

import com.fooddelivery.enums.Role;

public record UserResponse(
        Long id,
        String name,
        String contact,
        Role role,
        boolean deleted
) {
}