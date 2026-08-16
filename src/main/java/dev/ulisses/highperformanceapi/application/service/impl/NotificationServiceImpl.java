package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.gateway.EmailNotificationSender;
import dev.ulisses.highperformanceapi.application.service.NotificationService;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Notification;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.enums.NotificationStatus;
import dev.ulisses.highperformanceapi.domain.enums.NotificationType;
import dev.ulisses.highperformanceapi.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailNotificationSender emailNotificationSender;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            EmailNotificationSender emailNotificationSender
    ) {
        this.notificationRepository = notificationRepository;
        this.emailNotificationSender = emailNotificationSender;
    }

    @Override
    public Notification create(
            Customer customer,
            Order order,
            String subject,
            String message
    ) {

        Notification notification = new Notification();

        notification.setCustomer(customer);
        notification.setOrder(order);
        notification.setType(NotificationType.EMAIL);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setSubject(subject);
        notification.setMessage(message);

        return notificationRepository.save(notification);
    }

    @Override
    public Notification send(Notification notification) {

        try {

            emailNotificationSender.send(notification);

            notification.setStatus(NotificationStatus.SENT);

        } catch (RuntimeException ex) {

            notification.setStatus(NotificationStatus.FAILED);
        }

        return notificationRepository.save(notification);
    }
}
