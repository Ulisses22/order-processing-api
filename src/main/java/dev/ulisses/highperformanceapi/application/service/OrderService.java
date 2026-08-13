package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreateOrderRequest;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse create(CreateOrderRequest request);

    OrderResponse findById(UUID id);

    Page<OrderResponse> findAll(Pageable pageable);

    OrderResponse cancel(UUID id);

    OrderResponse updateStatus(UUID id, OrderStatus status);
}
