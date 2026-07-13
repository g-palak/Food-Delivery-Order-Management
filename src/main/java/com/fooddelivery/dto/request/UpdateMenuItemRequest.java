package com.fooddelivery.dto.request;

import com.fooddelivery.enums.MenuItemAvailability;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateMenuItemRequest(
        @Size(max = 150)
        String name,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @Min(value = 0, message = "Stock cannot be negative")
        Integer stockQuantity,

        MenuItemAvailability availability
) {
}