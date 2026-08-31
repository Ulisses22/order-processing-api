package dev.ulisses.highperformanceapi.application.gateway;

import dev.ulisses.highperformanceapi.domain.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FakeEmailNotificationSender implements EmailNotificationSender {

    private static final Logger log =
            LoggerFactory.getLogger(FakeEmailNotificationSender.class);

    @Override
    public void send(Notification notification) {

        log.info(
                "Sending email notification: customerId={}, subject={}, message={}",
                notification.getCustomer().getId(),
                notification.getSubject(),
                notification.getMessage()
        );
    }
}