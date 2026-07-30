package dev.ulisses.highperformanceapi.application.dto.response;

import dev.ulisses.highperformanceapi.domain.enums.NotificationStatus;
import dev.ulisses.highperformanceapi.domain.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(

        UUID id,

        UUID customerId,

        UUID orderId,

        NotificationType type,

        NotificationStatus status,

        String subject,

        String message,

        Instant createdAt,

        Instant updatedAt

) {
}
