package dev.ulisses.highperformanceapi.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(

        UUID id,

        UUID productId,

        Integer availableQuantity,

        Integer reservedQuantity,

        Instant createdAt,

        Instant updatedAt

) {
}
