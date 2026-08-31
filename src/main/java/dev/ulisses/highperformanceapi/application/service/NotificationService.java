package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Notification;
import dev.ulisses.highperformanceapi.domain.entity.Order;

public interface NotificationService {

    Notification create(
            Customer customer,
            Order order,
            String subject,
            String message
    );

    Notification send(Notification notification);
}
