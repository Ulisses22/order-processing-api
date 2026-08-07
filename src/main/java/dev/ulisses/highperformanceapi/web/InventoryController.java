package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.InventoryQuantityRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateInventoryRequest;
import dev.ulisses.highperformanceapi.application.dto.response.InventoryResponse;
import dev.ulisses.highperformanceapi.application.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(
                inventoryService.getByProductId(productId)
        );
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateInventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.updateStock(productId, request)
        );
    }

    @PostMapping("/products/{productId}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable UUID productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        inventoryService.reserveStock(productId, request.quantity());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/products/{productId}/release")
    public ResponseEntity<Void> releaseStock(
            @PathVariable UUID productId,
            @Valid @RequestBody InventoryQuantityRequest request) {

        inventoryService.releaseStock(productId, request.quantity());

        return ResponseEntity.ok().build();
    }
}
