package dev.ulisses.highperformanceapi.application.dto.request;

import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {
}
