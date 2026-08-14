package dev.ulisses.highperformanceapi.application.event;


public interface PaymentEventPublisher {

    void publish(PaymentAuthorizedEvent event);

    void publish(PaymentFailedEvent event);
}