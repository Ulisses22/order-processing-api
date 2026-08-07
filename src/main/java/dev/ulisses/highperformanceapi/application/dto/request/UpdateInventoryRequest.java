package dev.ulisses.highperformanceapi.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateInventoryRequest(

        @NotNull(message = "Available quantity is required.")
        @PositiveOrZero(message = "Available quantity must be zero or greater.")
        Integer availableQuantity,

        @NotNull(message = "Reserved quantity is required.")
        @PositiveOrZero(message = "Reserved quantity must be zero or greater.")
        Integer reservedQuantity

) {
}
