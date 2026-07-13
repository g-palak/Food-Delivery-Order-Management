package com.fooddelivery.dto.request;

import com.fooddelivery.enums.MenuItemAvailability;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 150)
        String name,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stockQuantity,

        @NotNull(message = "Availability is required")
        MenuItemAvailability availability
) {
}