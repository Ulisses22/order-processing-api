package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.CreateShipmentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ShipmentResponse;
import dev.ulisses.highperformanceapi.application.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(
            @Valid @RequestBody CreateShipmentRequest request
    ) {

        ShipmentResponse response = shipmentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> findById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                shipmentService.findById(id)
        );
    }
}
