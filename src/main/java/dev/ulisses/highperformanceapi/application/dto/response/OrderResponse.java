package dev.ulisses.highperformanceapi.application.dto.response;

import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(

        UUID id,

        String orderNumber,

        UUID customerId,

        OrderStatus status,

        BigDecimal totalAmount,

        List<OrderItemResponse> items,

        Instant createdAt,

        Instant updatedAt

) {
}
