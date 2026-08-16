package dev.ulisses.highperformanceapi.application.gateway;

import dev.ulisses.highperformanceapi.domain.entity.Notification;

public interface EmailNotificationSender {

    void send(Notification notification);
}