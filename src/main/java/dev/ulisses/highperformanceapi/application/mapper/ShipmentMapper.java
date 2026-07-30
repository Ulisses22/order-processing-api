package dev.ulisses.highperformanceapi.application.mapper;

import dev.ulisses.highperformanceapi.application.dto.request.CreateShipmentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ShipmentResponse;
import dev.ulisses.highperformanceapi.domain.entity.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ShipmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "shippedAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    Shipment toEntity(CreateShipmentRequest request);

    @Mapping(target = "orderId", source = "order.id")
    ShipmentResponse toResponse(Shipment shipment);

}
