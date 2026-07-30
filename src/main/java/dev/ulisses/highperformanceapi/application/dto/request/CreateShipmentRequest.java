package dev.ulisses.highperformanceapi.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateShipmentRequest(

        @NotNull(message = "Order id is required.")
        UUID orderId,

        @NotBlank(message = "Carrier is required.")
        @Size(max = 100, message = "Carrier must not exceed 100 characters.")
        String carrier

) {
}