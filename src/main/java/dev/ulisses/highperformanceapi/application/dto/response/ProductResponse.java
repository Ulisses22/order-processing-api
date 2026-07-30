package dev.ulisses.highperformanceapi.application.dto.response;

import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(

        UUID id,

        String sku,

        String name,

        BigDecimal price,

        ProductStatus status,

        Instant createdAt,

        Instant updatedAt

) {
}
