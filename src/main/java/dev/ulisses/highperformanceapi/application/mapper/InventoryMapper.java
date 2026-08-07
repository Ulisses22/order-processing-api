package dev.ulisses.highperformanceapi.application.mapper;

import dev.ulisses.highperformanceapi.application.dto.request.UpdateInventoryRequest;
import dev.ulisses.highperformanceapi.application.dto.response.InventoryResponse;
import dev.ulisses.highperformanceapi.domain.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface InventoryMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "availableQuantity", source = "availableQuantity")
    @Mapping(target = "reservedQuantity", source = "reservedQuantity")
    InventoryResponse toResponse(Inventory inventory);

    List<InventoryResponse> toResponseList(List<Inventory> inventories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateEntity(
            UpdateInventoryRequest request,
            @MappingTarget Inventory inventory
    );
}
