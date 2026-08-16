package dev.ulisses.highperformanceapi.application.event.listener;

import dev.ulisses.highperformanceapi.application.event.OrderCreatedEvent;
import dev.ulisses.highperformanceapi.application.event.PaymentAuthorizedEvent;
import dev.ulisses.highperformanceapi.application.event.ShipmentCreatedEvent;
import dev.ulisses.highperformanceapi.application.service.NotificationService;
import dev.ulisses.highperformanceapi.domain.entity.Notification;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final OrderRepository orderRepository;

    public NotificationEventListener(
            NotificationService notificationService,
            OrderRepository orderRepository
    ) {
        this.notificationService = notificationService;
        this.orderRepository = orderRepository;
    }

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {

        Order order = getOrder(event.orderId());

        Notification notification = notificationService.create(
                order.getCustomer(),
                order,
                "Order created",
                "Your order " + order.getOrderNumber()
                        + " has been created successfully."
        );

        send(notification);
    }

    @EventListener
    public void handlePaymentAuthorized(PaymentAuthorizedEvent event) {

        Order order = getOrder(event.orderId());

        Notification notification = notificationService.create(
                order.getCustomer(),
                order,
                "Payment received",
                "Your payment for order "
                        + order.getOrderNumber()
                        + " has been received successfully."
        );

        send(notification);
    }

    @EventListener
    public void handleShipmentCreated(ShipmentCreatedEvent event) {

        Order order = getOrder(event.orderId());

        Notification notification = notificationService.create(
                order.getCustomer(),
                order,
                "Shipment created",
                "Your order " + order.getOrderNumber()
                        + " has been shipped successfully."
        );

        send(notification);
    }

    private Order getOrder(java.util.UUID orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for notification: " + orderId
                ));
    }

    private void send(Notification notification) {
        notificationService.send(notification);
    }
}
