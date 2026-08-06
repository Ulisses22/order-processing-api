package dev.ulisses.highperformanceapi.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank(message = "SKU is required.")
        @Size(max = 100, message = "SKU must not exceed 100 characters.")
        String sku,

        @NotBlank(message = "Product name is required.")
        @Size(max = 255, message = "Product name must not exceed 255 characters.")
        String name,

        @Size(max = 1000)
        String description,

        @NotNull(message = "Price is required.")
        @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than zero.")
        BigDecimal price

) {
}
