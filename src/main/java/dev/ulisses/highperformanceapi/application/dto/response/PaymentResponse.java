package dev.ulisses.highperformanceapi.application.dto.response;

import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(

        UUID id,

        UUID orderId,

        BigDecimal amount,

        PaymentMethod paymentMethod,

        PaymentStatus status,

        String transactionId,

        Instant processedAt,

        Instant createdAt,

        Instant updatedAt

) {
}
