package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreateShipmentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ShipmentResponse;
import dev.ulisses.highperformanceapi.application.exception.BusinessException;
import dev.ulisses.highperformanceapi.application.exception.DuplicateResourceException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.ShipmentMapper;
import dev.ulisses.highperformanceapi.application.service.impl.ShipmentServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.entity.Shipment;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.ShipmentStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import dev.ulisses.highperformanceapi.domain.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ShipmentServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    @Test
    void shouldCreateShipmentSuccessfully() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                "DHL"
        );

        Order order = new Order();
        order.setId(orderId);
        order.setShippingAddress("Basel, Switzerland");
        order.setStatus(OrderStatus.PROCESSING);

        Shipment shipment = new Shipment();
        ShipmentResponse expectedResponse = new ShipmentResponse(
                UUID.randomUUID(),
                orderId,
                "DHL",
                "TRK-123",
                ShipmentStatus.PENDING,
                Instant.now(),
                Instant.now()
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(shipmentRepository.existsByOrderId(orderId))
                .thenReturn(false);

        when(shipmentMapper.toEntity(request))
                .thenReturn(shipment);

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response = shipmentService.create(request);

        // Assert

        assertThat(response).isEqualTo(expectedResponse);

        assertThat(shipment.getOrder()).isEqualTo(order);
        assertThat(shipment.getShippingAddress())
                .isEqualTo("Basel, Switzerland");
        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.PENDING);
        assertThat(shipment.getTrackingNumber())
                .startsWith("TRK-");

        verify(orderRepository).findById(orderId);
        verify(shipmentRepository).existsByOrderId(orderId);
        verify(shipmentRepository).save(shipment);
        verify(shipmentMapper).toEntity(request);
        verify(shipmentMapper).toResponse(shipment);
    }

    @Test
    void shouldCopyOrderShippingAddressAndInitializeShipment() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                "DHL"
        );

        Order order = new Order();
        order.setId(orderId);
        order.setShippingAddress("Basel, Switzerland");
        order.setStatus(OrderStatus.PROCESSING);

        Shipment shipment = new Shipment();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(shipmentRepository.existsByOrderId(orderId))
                .thenReturn(false);

        when(shipmentMapper.toEntity(request))
                .thenReturn(shipment);

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(null);

        // Act

        shipmentService.create(request);

        // Assert

        assertThat(shipment.getOrder())
                .isEqualTo(order);

        assertThat(shipment.getShippingAddress())
                .isEqualTo(order.getShippingAddress());

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.PENDING);

        assertThat(shipment.getTrackingNumber())
                .isNotBlank()
                .startsWith("TRK-");

        verify(shipmentRepository).save(shipment);
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                "DHL"
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThatThrownBy(() -> shipmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with id: " + orderId);

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(shipmentRepository);
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldThrowExceptionWhenShipmentAlreadyExists() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                "DHL"
        );

        Order order = new Order();
        order.setId(orderId);
        order.setShippingAddress("Basel, Switzerland");
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(shipmentRepository.existsByOrderId(orderId))
                .thenReturn(true);

        // Act & Assert

        assertThatThrownBy(() -> shipmentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Shipment already exists for order: " + orderId);

        verify(orderRepository).findById(orderId);
        verify(shipmentRepository).existsByOrderId(orderId);
        verify(shipmentRepository, never()).save(any());
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldThrowExceptionWhenOrderIsCancelled() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                "DHL"
        );

        Order order = new Order();
        order.setId(orderId);
        order.setShippingAddress("Basel, Switzerland");
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act & Assert

        assertThatThrownBy(() -> shipmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Shipment cannot be created for a cancelled order.");

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(shipmentRepository);
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldThrowExceptionWhenOrderIsRefunded() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                "DHL"
        );

        Order order = new Order();
        order.setId(orderId);
        order.setShippingAddress("Basel, Switzerland");
        order.setStatus(OrderStatus.REFUNDED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act & Assert

        assertThatThrownBy(() -> shipmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Shipment cannot be created for a refunded order.");

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(shipmentRepository);
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldThrowExceptionWhenOrderIsDelivered() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                "DHL"
        );

        Order order = new Order();
        order.setId(orderId);
        order.setShippingAddress("Basel, Switzerland");
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act & Assert

        assertThatThrownBy(() -> shipmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Shipment cannot be created for a delivered order.");

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(shipmentRepository);
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldFindShipmentByIdSuccessfully() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Shipment shipment = new Shipment();

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                orderId,
                "DHL",
                "TRK-123",
                ShipmentStatus.PENDING,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response = shipmentService.findById(shipmentId);

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        verify(shipmentRepository)
                .findById(shipmentId);

        verify(shipmentMapper)
                .toResponse(shipment);
    }

    @Test
    void shouldThrowExceptionWhenShipmentDoesNotExist() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThatThrownBy(() -> shipmentService.findById(shipmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Shipment not found with id: " + shipmentId);

        verify(shipmentRepository)
                .findById(shipmentId);

        verifyNoInteractions(shipmentMapper);
    }

    // status-transition unit tests
    @Test
    void shouldUpdateShipmentStatusToPreparing() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PENDING);

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                UUID.randomUUID(),
                "DHL",
                "TRK-123",
                ShipmentStatus.PREPARING,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response =
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.PREPARING
                );

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.PREPARING);

        verify(shipmentRepository)
                .findById(shipmentId);

        verify(shipmentRepository)
                .save(shipment);

        verify(shipmentMapper)
                .toResponse(shipment);
    }

    @Test
    void shouldUpdateShipmentStatusToShipped() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PREPARING);

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                UUID.randomUUID(),
                "DHL",
                "TRK-123",
                ShipmentStatus.SHIPPED,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response =
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.SHIPPED
                );

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.SHIPPED);

        assertThat(shipment.getShippedAt())
                .isNotNull();

        assertThat(shipment.getDeliveredAt())
                .isNull();

        verify(shipmentRepository)
                .findById(shipmentId);

        verify(shipmentRepository)
                .save(shipment);

        verify(shipmentMapper)
                .toResponse(shipment);
    }

    @Test
    void shouldUpdateShipmentStatusToInTransit() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setShippedAt(Instant.now());

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                UUID.randomUUID(),
                "DHL",
                "TRK-123",
                ShipmentStatus.IN_TRANSIT,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response =
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.IN_TRANSIT
                );

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.IN_TRANSIT);

        assertThat(shipment.getShippedAt())
                .isNotNull();

        assertThat(shipment.getDeliveredAt())
                .isNull();

        verify(shipmentRepository)
                .findById(shipmentId);

        verify(shipmentRepository)
                .save(shipment);

        verify(shipmentMapper)
                .toResponse(shipment);
    }

    @Test
    void shouldUpdateShipmentStatusToDelivered() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Instant shippedAt = Instant.now().minusSeconds(3600);

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setShippedAt(shippedAt);

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                UUID.randomUUID(),
                "DHL",
                "TRK-123",
                ShipmentStatus.DELIVERED,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response =
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.DELIVERED
                );

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.DELIVERED);

        assertThat(shipment.getShippedAt())
                .isEqualTo(shippedAt);

        assertThat(shipment.getDeliveredAt())
                .isNotNull();

        assertThat(shipment.getDeliveredAt())
                .isAfterOrEqualTo(shippedAt);

        verify(shipmentRepository)
                .findById(shipmentId);

        verify(shipmentRepository)
                .save(shipment);

        verify(shipmentMapper)
                .toResponse(shipment);
    }

    @Test
    void shouldUpdateShipmentStatusToReturned() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Instant shippedAt = Instant.now().minusSeconds(3600);

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setShippedAt(shippedAt);

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                UUID.randomUUID(),
                "DHL",
                "TRK-123",
                ShipmentStatus.RETURNED,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response =
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.RETURNED
                );

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.RETURNED);

        assertThat(shipment.getShippedAt())
                .isEqualTo(shippedAt);

        assertThat(shipment.getDeliveredAt())
                .isNull();

        verify(shipmentRepository)
                .findById(shipmentId);

        verify(shipmentRepository)
                .save(shipment);

        verify(shipmentMapper)
                .toResponse(shipment);
    }

    @Test
    void shouldUpdateShipmentStatusToReturnedFromInTransit() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Instant shippedAt = Instant.now().minusSeconds(3600);

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setShippedAt(shippedAt);

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                UUID.randomUUID(),
                "DHL",
                "TRK-123",
                ShipmentStatus.RETURNED,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response =
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.RETURNED
                );

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.RETURNED);

        assertThat(shipment.getShippedAt())
                .isEqualTo(shippedAt);

        assertThat(shipment.getDeliveredAt())
                .isNull();

        verify(shipmentRepository)
                .findById(shipmentId);

        verify(shipmentRepository)
                .save(shipment);

        verify(shipmentMapper)
                .toResponse(shipment);
    }

    @Test
    void shouldUpdateShipmentStatusToCancelled() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PENDING);

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                UUID.randomUUID(),
                "DHL",
                "TRK-123",
                ShipmentStatus.CANCELLED,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response =
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.CANCELLED
                );

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.CANCELLED);

        assertThat(shipment.getShippedAt())
                .isNull();

        assertThat(shipment.getDeliveredAt())
                .isNull();

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository).save(shipment);
        verify(shipmentMapper).toResponse(shipment);
    }

    @Test
    void shouldUpdateShipmentStatusToCancelledFromPreparing() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PREPARING);

        ShipmentResponse expectedResponse = new ShipmentResponse(
                shipmentId,
                UUID.randomUUID(),
                "DHL",
                "TRK-123",
                ShipmentStatus.CANCELLED,
                Instant.now(),
                Instant.now()
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(expectedResponse);

        // Act

        ShipmentResponse response =
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.CANCELLED
                );

        // Assert

        assertThat(response)
                .isEqualTo(expectedResponse);

        assertThat(shipment.getStatus())
                .isEqualTo(ShipmentStatus.CANCELLED);

        assertThat(shipment.getShippedAt())
                .isNull();

        assertThat(shipment.getDeliveredAt())
                .isNull();

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository).save(shipment);
        verify(shipmentMapper).toResponse(shipment);
    }

    // invalid transitions
    @Test
    void shouldThrowExceptionWhenPendingShipmentTransitionsToDelivered() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PENDING);

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        // Act & Assert

        assertThatThrownBy(() ->
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.DELIVERED
                ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(
                        "Invalid status transition from PENDING to DELIVERED"
                );

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository, never()).save(any());
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldThrowExceptionWhenDeliveredShipmentTransitionsToInTransit() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.DELIVERED);

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        // Act & Assert

        assertThatThrownBy(() ->
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.IN_TRANSIT
                ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Delivered shipments cannot be updated.");

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository, never()).save(any());
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldThrowExceptionWhenCancelledShipmentTransitionsToShipped() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.CANCELLED);

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        // Act & Assert

        assertThatThrownBy(() ->
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.SHIPPED
                ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cancelled shipments cannot be updated.");

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository, never()).save(any());
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldThrowExceptionWhenShipmentIsAlreadyInSameStatus() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.PENDING);

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        // Act & Assert

        assertThatThrownBy(() ->
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.PENDING
                ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Shipment is already in status: PENDING");

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository, never()).save(any());
        verifyNoInteractions(shipmentMapper);
    }

    @Test
    void shouldThrowExceptionWhenShipmentDoesNotExistDuringStatusUpdate() {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThatThrownBy(() ->
                shipmentService.updateStatus(
                        shipmentId,
                        ShipmentStatus.PREPARING
                ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Shipment not found with id: " + shipmentId);

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository, never()).save(any());
        verifyNoInteractions(shipmentMapper);
    }

}

