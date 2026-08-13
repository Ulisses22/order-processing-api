package dev.ulisses.highperformanceapi.web;


import dev.ulisses.highperformanceapi.application.dto.request.CreateOrderRequest;
import dev.ulisses.highperformanceapi.application.dto.request.OrderSearchRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateOrderStatusRequest;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.application.service.OrderService;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order")
    public OrderResponse create(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.create(request);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable UUID id) {

        return orderService.findById(id);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public OrderResponse cancel(@PathVariable UUID id) {

        return orderService.cancel(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public OrderResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {

        return orderService.updateStatus(
                id,
                request.status()
        );
    }

    @GetMapping
    public Page<OrderResponse> search(

            @RequestParam(required = false) UUID customerId,

            @RequestParam(required = false) OrderStatus status,

            @RequestParam(required = false) String orderNumber,

            @RequestParam(required = false) Instant createdFrom,

            @RequestParam(required = false) Instant createdTo,

            Pageable pageable
    ) {

        OrderSearchRequest request = new OrderSearchRequest(
                customerId,
                status,
                orderNumber,
                createdFrom,
                createdTo
        );

        return orderService.search(request, pageable);
    }
}
