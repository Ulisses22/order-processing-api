package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.dto.request.CreateShipmentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ShipmentResponse;
import dev.ulisses.highperformanceapi.application.exception.BusinessException;
import dev.ulisses.highperformanceapi.application.exception.DuplicateResourceException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.ShipmentMapper;
import dev.ulisses.highperformanceapi.application.service.ShipmentService;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.entity.Shipment;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.ShipmentStatus;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import dev.ulisses.highperformanceapi.domain.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentMapper shipmentMapper;

    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository,
            ShipmentMapper shipmentMapper
    ) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.shipmentMapper = shipmentMapper;
    }

    @Override
    public ShipmentResponse create(CreateShipmentRequest request) {

        Order order = getOrder(request.orderId());

        validateOrderCanBeShipped(order);

        validateShipmentDoesNotExist(order.getId());

        Shipment shipment = shipmentMapper.toEntity(request);

        shipment.setOrder(order);
        shipment.setShippingAddress(order.getShippingAddress());
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setStatus(ShipmentStatus.PENDING);

        Shipment savedShipment = shipmentRepository.save(shipment);

        return shipmentMapper.toResponse(savedShipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse findById(UUID id) {

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with id: " + id
                ));

        return shipmentMapper.toResponse(shipment);
    }

    private Order getOrder(UUID orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));
    }

    private void validateShipmentDoesNotExist(UUID orderId) {

        if (shipmentRepository.existsByOrderId(orderId)) {
            throw new DuplicateResourceException(
                    "Shipment already exists for order: " + orderId
            );
        }
    }

    private void validateOrderCanBeShipped(Order order) {

        OrderStatus status = order.getStatus();

        if (status == OrderStatus.CANCELLED) {
            throw new BusinessException(
                    "Shipment cannot be created for a cancelled order."
            );
        }

        if (status == OrderStatus.REFUNDED) {
            throw new BusinessException(
                    "Shipment cannot be created for a refunded order."
            );
        }

        if (status == OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Shipment cannot be created for a delivered order."
            );
        }
    }

    private String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID();
    }
}
