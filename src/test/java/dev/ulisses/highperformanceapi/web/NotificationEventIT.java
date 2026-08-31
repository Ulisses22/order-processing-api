package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.event.OrderCreatedEvent;
import dev.ulisses.highperformanceapi.application.event.PaymentAuthorizedEvent;
import dev.ulisses.highperformanceapi.application.event.ShipmentCreatedEvent;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Notification;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.enums.NotificationStatus;
import dev.ulisses.highperformanceapi.domain.enums.NotificationType;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import dev.ulisses.highperformanceapi.domain.repository.NotificationRepository;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NotificationEventIT extends IntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void shouldPersistNotificationWhenOrderCreatedEventIsPublished() {

        Customer customer = createCustomer();
        Order order = createOrder(customer);

        eventPublisher.publishEvent(
                new OrderCreatedEvent(order.getId())
        );

        Notification notification =
                notificationRepository.findByOrderId(order.getId())
                        .stream()
                        .findFirst()
                        .orElseThrow();

        assertNotification(
                notification,
                customer,
                order,
                NotificationStatus.SENT,
                "Order created"
        );
    }

    @Test
    void shouldPersistNotificationWhenPaymentAuthorizedEventIsPublished() {

        Customer customer = createCustomer();
        Order order = createOrder(customer);

        eventPublisher.publishEvent(
                new PaymentAuthorizedEvent(
                        UUID.randomUUID(),
                        order.getId()
                )
        );

        Notification notification =
                notificationRepository.findByOrderId(order.getId())
                        .stream()
                        .findFirst()
                        .orElseThrow();

        assertNotification(
                notification,
                customer,
                order,
                NotificationStatus.SENT,
                "Payment received"
        );
    }

    @Test
    void shouldPersistNotificationWhenShipmentCreatedEventIsPublished() {

        Customer customer = createCustomer();
        Order order = createOrder(customer);

        eventPublisher.publishEvent(
                new ShipmentCreatedEvent(
                        UUID.randomUUID(),
                        order.getId()
                )
        );

        Notification notification =
                notificationRepository.findByOrderId(order.getId())
                        .stream()
                        .findFirst()
                        .orElseThrow();

        assertNotification(
                notification,
                customer,
                order,
                NotificationStatus.SENT,
                "Shipment created"
        );
    }

    private Customer createCustomer() {

        Customer customer = new Customer();

        customer.setFirstName("Notification");
        customer.setLastName("Test");
        customer.setEmail(
                "notification-" + UUID.randomUUID() + "@example.com"
        );
        customer.setStatus(CustomerStatus.ACTIVE);

        return customerRepository.save(customer);
    }

    private Order createOrder(Customer customer) {

        Order order = new Order();

        order.setCustomer(customer);
        order.setOrderNumber("ORDER-" + UUID.randomUUID());
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setShippingAddress("Basel, Switzerland");
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    private void assertNotification(
            Notification notification,
            Customer customer,
            Order order,
            NotificationStatus expectedStatus,
            String expectedSubject
    ) {

        assertThat(notification.getCustomer().getId())
                .isEqualTo(customer.getId());

        assertThat(notification.getOrder().getId())
                .isEqualTo(order.getId());

        assertThat(notification.getType())
                .isEqualTo(NotificationType.EMAIL);

        assertThat(notification.getStatus())
                .isEqualTo(expectedStatus);

        assertThat(notification.getSubject())
                .isEqualTo(expectedSubject);

        assertThat(notification.getMessage())
                .isNotBlank();
    }
}
