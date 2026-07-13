package com.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CityRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Country is required")
        @Size(max = 100)
        String country,

        @NotBlank(message = "State is required")
        @Size(max = 100)
        String state,

        @NotNull(message = "Longitude is required")
        BigDecimal longitude,

        @NotNull(message = "Latitude is required")
        BigDecimal latitude
) {
}