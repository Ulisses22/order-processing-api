package dev.ulisses.highperformanceapi.application.event;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID paymentId
) {
}
