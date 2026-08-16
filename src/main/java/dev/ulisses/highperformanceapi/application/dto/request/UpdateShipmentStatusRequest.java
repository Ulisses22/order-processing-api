package dev.ulisses.highperformanceapi.application.dto.request;

import dev.ulisses.highperformanceapi.domain.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(
        @NotNull(message = "Shipment status is required.") ShipmentStatus status
) {
}
