package dev.ulisses.highperformanceapi.application.event;

import java.util.UUID;

public record ShipmentCreatedEvent(
        UUID shipmentId,
        UUID orderId
) {
}