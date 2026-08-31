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

import dev.ulisses.highperformanceapi.application.event.ShipmentCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentMapper shipmentMapper;
    private final ApplicationEventPublisher eventPublisher;

    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository,
            ShipmentMapper shipmentMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.shipmentMapper = shipmentMapper;
        this.eventPublisher =  eventPublisher;
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

        eventPublisher.publishEvent(
                new ShipmentCreatedEvent(
                        savedShipment.getId(),
                        order.getId()
                )
        );

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

    // TODO: make it utils class
    private String generateTrackingNumber() {
        return "TRK-" + UUID.randomUUID();
    }

    @Override
    public ShipmentResponse updateStatus(UUID id, ShipmentStatus newStatus) {

        Shipment shipment = getShipment(id);

        validateStatusTransition(shipment, newStatus);

        shipment.setStatus(newStatus);

        if (newStatus == ShipmentStatus.SHIPPED) {
            shipment.setShippedAt(java.time.Instant.now());
        }

        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(java.time.Instant.now());
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);

        return shipmentMapper.toResponse(updatedShipment);
    }

    private Shipment getShipment(UUID shipmentId) {

        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with id: " + shipmentId
                ));
    }

    private void validateStatusTransition(
            Shipment shipment,
            ShipmentStatus newStatus
    ) {

        ShipmentStatus currentStatus = shipment.getStatus();

        if (currentStatus == newStatus) {
            throw new BusinessException(
                    "Shipment is already in status: " + currentStatus
            );
        }

        if (currentStatus == ShipmentStatus.DELIVERED) {
            throw new BusinessException(
                    "Delivered shipments cannot be updated."
            );
        }

        if (currentStatus == ShipmentStatus.CANCELLED) {
            throw new BusinessException(
                    "Cancelled shipments cannot be updated."
            );
        }

        boolean validTransition = switch (currentStatus) {

            case PENDING ->
                    newStatus == ShipmentStatus.PREPARING
                            || newStatus == ShipmentStatus.CANCELLED;

            case PREPARING ->
                    newStatus == ShipmentStatus.SHIPPED
                            || newStatus == ShipmentStatus.CANCELLED;

            case SHIPPED ->
                    newStatus == ShipmentStatus.IN_TRANSIT
                            || newStatus == ShipmentStatus.RETURNED;

            case IN_TRANSIT ->
                    newStatus == ShipmentStatus.DELIVERED
                            || newStatus == ShipmentStatus.RETURNED;

            default -> false;
        };

        if (!validTransition) {
            throw new BusinessException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }
}
