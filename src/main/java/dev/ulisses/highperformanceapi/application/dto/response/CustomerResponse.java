package dev.ulisses.highperformanceapi.application.dto.response;

import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        CustomerStatus status,

        Instant createdAt,

        Instant updatedAt

) {
}
