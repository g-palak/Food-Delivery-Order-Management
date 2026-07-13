package com.fooddelivery.dto.request;

import com.fooddelivery.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Contact is required")
        @Size(max = 20)
        String contact,

        @NotNull(message = "Role is required")
        Role role
) {
}