package com.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RestaurantRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String address,

        @NotNull(message = "CityId is required")
        Long cityId,

        @NotNull(message = "OwnerId is required")
        Long ownerId
) {
}