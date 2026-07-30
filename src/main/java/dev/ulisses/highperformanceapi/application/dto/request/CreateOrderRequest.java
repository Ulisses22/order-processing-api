package dev.ulisses.highperformanceapi.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(

        @NotNull(message = "Customer id is required.")
        UUID customerId,

        @NotEmpty(message = "Order must contain at least one item.")
        List<@Valid OrderItemRequest> items

) {
}
