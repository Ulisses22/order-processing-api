package dev.ulisses.highperformanceapi.application.dto.response;

import dev.ulisses.highperformanceapi.domain.enums.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

public record ShipmentResponse(

        UUID id,

        UUID orderId,

        String carrier,

        String trackingNumber,

        ShipmentStatus status,

        Instant createdAt,

        Instant updatedAt

) {
}
