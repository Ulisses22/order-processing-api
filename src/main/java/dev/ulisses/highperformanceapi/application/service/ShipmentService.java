package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreateShipmentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ShipmentResponse;
import dev.ulisses.highperformanceapi.domain.enums.ShipmentStatus;

import java.util.UUID;

public interface ShipmentService {

    ShipmentResponse create(CreateShipmentRequest request);

    ShipmentResponse findById(UUID id);

    ShipmentResponse updateStatus(UUID id, ShipmentStatus newStatus);
}
