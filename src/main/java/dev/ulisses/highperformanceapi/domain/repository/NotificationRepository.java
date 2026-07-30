package dev.ulisses.highperformanceapi.domain.repository;

import dev.ulisses.highperformanceapi.domain.entity.Notification;
import dev.ulisses.highperformanceapi.domain.enums.NotificationStatus;
import dev.ulisses.highperformanceapi.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByCustomerId(UUID customerId);

    List<Notification> findByOrderId(UUID orderId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByType(NotificationType type);

}
