package dev.ulisses.highperformanceapi.application.gateway;

import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Notification;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.enums.NotificationStatus;
import dev.ulisses.highperformanceapi.domain.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class FakeEmailNotificationSenderTest {

    private FakeEmailNotificationSender emailNotificationSender;

    @BeforeEach
    void setUp() {
        emailNotificationSender = new FakeEmailNotificationSender();
    }

    @Test
    void shouldSendEmailNotification() {

        Customer customer = new Customer();
        Order order = new Order();

        Notification notification = new Notification();

        notification.setCustomer(customer);
        notification.setOrder(order);
        notification.setType(NotificationType.EMAIL);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setSubject("Order created");
        notification.setMessage(
                "Your order has been created successfully."
        );

        assertThatCode(() ->
                emailNotificationSender.send(notification)
        ).doesNotThrowAnyException();
    }
}
