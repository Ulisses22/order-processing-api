package dev.ulisses.highperformanceapi.application.dto.request;

import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderSearchRequest(

        UUID customerId,
        OrderStatus status,
        String orderNumber,
        Instant createdFrom,
        Instant createdTo
) {
}
