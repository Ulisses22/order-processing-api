package dev.ulisses.highperformanceapi.application.event.listener;

import dev.ulisses.highperformanceapi.application.event.OrderCreatedEvent;
import dev.ulisses.highperformanceapi.application.event.PaymentAuthorizedEvent;
import dev.ulisses.highperformanceapi.application.event.ShipmentCreatedEvent;
import dev.ulisses.highperformanceapi.application.service.NotificationService;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Notification;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderRepository orderRepository;

    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(
                notificationService,
                orderRepository
        );
    }

    @Test
    void shouldCreateAndSendNotificationWhenOrderIsCreated() {

        UUID orderId = UUID.randomUUID();

        Customer customer = new Customer();
        Order order = new Order();

        order.setCustomer(customer);
        order.setOrderNumber("ORDER-123");

        Notification notification = new Notification();

        when(orderRepository.findById(orderId))
                .thenReturn(java.util.Optional.of(order));

        when(notificationService.create(
                customer,
                order,
                "Order created",
                "Your order ORDER-123 has been created successfully."
        )).thenReturn(notification);

        listener.handleOrderCreated(
                new OrderCreatedEvent(orderId)
        );

        verify(notificationService).create(
                customer,
                order,
                "Order created",
                "Your order ORDER-123 has been created successfully."
        );

        verify(notificationService).send(notification);
    }

    @Test
    void shouldCreateAndSendNotificationWhenPaymentIsAuthorized() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Customer customer = new Customer();
        Order order = new Order();

        order.setCustomer(customer);
        order.setOrderNumber("ORDER-456");

        Notification notification = new Notification();

        when(orderRepository.findById(orderId))
                .thenReturn(java.util.Optional.of(order));

        when(notificationService.create(
                customer,
                order,
                "Payment received",
                "Your payment for order ORDER-456 has been received successfully."
        )).thenReturn(notification);

        listener.handlePaymentAuthorized(
                new PaymentAuthorizedEvent(
                        paymentId,
                        orderId
                )
        );

        verify(notificationService).create(
                customer,
                order,
                "Payment received",
                "Your payment for order ORDER-456 has been received successfully."
        );

        verify(notificationService).send(notification);
    }

    @Test
    void shouldNotSendNotificationWhenOrderDoesNotExist() {

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                listener.handleOrderCreated(
                        new OrderCreatedEvent(orderId)
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order not found");

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldCreateAndSendNotificationWhenShipmentIsCreated() {

        UUID shipmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Customer customer = new Customer();
        Order order = new Order();

        order.setCustomer(customer);
        order.setOrderNumber("ORDER-789");

        Notification notification = new Notification();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(notificationService.create(
                customer,
                order,
                "Shipment created",
                "Your order ORDER-789 has been shipped successfully."
        )).thenReturn(notification);

        listener.handleShipmentCreated(
                new ShipmentCreatedEvent(
                        shipmentId,
                        orderId
                )
        );

        verify(notificationService).create(
                customer,
                order,
                "Shipment created",
                "Your order ORDER-789 has been shipped successfully."
        );

        verify(notificationService).send(notification);
    }

}
