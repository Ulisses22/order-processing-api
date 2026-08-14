package dev.ulisses.highperformanceapi.application.event.listener;

import dev.ulisses.highperformanceapi.application.event.PaymentAuthorizedEvent;
import dev.ulisses.highperformanceapi.application.event.PaymentFailedEvent;
import dev.ulisses.highperformanceapi.application.service.OrderService;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final OrderService orderService;

    public PaymentEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    public void handlePaymentAuthorized(PaymentAuthorizedEvent event) {

        // The payment event only carries the payment ID.
        // The order relationship is resolved by the application layer.
        // We will add this lookup through the payment service/repository
        // rather than coupling the publisher to OrderService.
        orderService.updateStatus(
                event.orderId(),
                OrderStatus.PROCESSING
        );
    }
}
