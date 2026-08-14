package dev.ulisses.highperformanceapi.application.event;

import java.util.UUID;

public record PaymentAuthorizedEvent(
        UUID paymentId,
        UUID orderId
) {
}
