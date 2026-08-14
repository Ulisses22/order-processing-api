package dev.ulisses.highperformanceapi.application.dto.request;

import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentSearchRequest(

        UUID orderId,

        PaymentStatus status,

        PaymentMethod paymentMethod,

        String transactionId,

        Instant createdFrom,

        Instant createdTo

) {
}