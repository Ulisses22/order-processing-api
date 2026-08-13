package dev.ulisses.highperformanceapi.application.event;

import java.util.UUID;

public record OrderCreatedEvent(UUID orderId) {}
