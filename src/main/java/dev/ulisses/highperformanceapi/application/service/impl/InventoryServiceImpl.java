package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.dto.request.UpdateInventoryRequest;
import dev.ulisses.highperformanceapi.application.dto.response.InventoryResponse;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.InventoryMapper;
import dev.ulisses.highperformanceapi.application.service.InventoryService;
import dev.ulisses.highperformanceapi.domain.entity.Inventory;
import dev.ulisses.highperformanceapi.domain.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public InventoryResponse getByProductId(UUID productId) {
        Inventory inventory = findInventoryByProductId(productId);
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    public InventoryResponse updateStock(UUID productId, UpdateInventoryRequest request) {

        Inventory inventory = findInventoryByProductId(productId);

        inventory.setAvailableQuantity(request.availableQuantity());
        inventory.setReservedQuantity(request.reservedQuantity());

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return inventoryMapper.toResponse(updatedInventory);
    }

    @Override
    public void reserveStock(UUID productId, int quantity) {

        Inventory inventory = findInventoryByProductId(productId);

        inventory.reserve(quantity);

        inventoryRepository.save(inventory);
    }

    @Override
    public void releaseStock(UUID productId, int quantity) {

        Inventory inventory = findInventoryByProductId(productId);

        inventory.release(quantity);

        inventoryRepository.save(inventory);
    }

    private Inventory findInventoryByProductId(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
    }
}