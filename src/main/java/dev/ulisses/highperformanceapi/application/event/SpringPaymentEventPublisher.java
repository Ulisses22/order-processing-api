package dev.ulisses.highperformanceapi.application.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringPaymentEventPublisher implements PaymentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public SpringPaymentEventPublisher(
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(PaymentAuthorizedEvent event) {
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publish(PaymentFailedEvent event) {
        eventPublisher.publishEvent(event);
    }
}
