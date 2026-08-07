package dev.ulisses.highperformanceapi.application.service;


import dev.ulisses.highperformanceapi.application.dto.request.UpdateInventoryRequest;
import dev.ulisses.highperformanceapi.application.dto.response.InventoryResponse;

import java.util.UUID;

public interface InventoryService {

    InventoryResponse getByProductId(UUID productId);

    InventoryResponse updateStock(UUID productId, UpdateInventoryRequest request);

    void reserveStock(UUID productId, int quantity);

    void releaseStock(UUID productId, int quantity);
}
