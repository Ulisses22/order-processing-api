package dev.ulisses.highperformanceapi.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(

        UUID id,

        UUID productId,

        String productName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal subtotal

) {
}
