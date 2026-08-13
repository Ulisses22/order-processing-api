package dev.ulisses.highperformanceapi.web;


import dev.ulisses.highperformanceapi.application.dto.request.CreateOrderRequest;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.application.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public Page<OrderResponse> findAll(Pageable pageable) {

        return orderService.findAll(pageable);
    }
}
