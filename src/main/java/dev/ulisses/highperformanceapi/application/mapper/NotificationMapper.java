package dev.ulisses.highperformanceapi.application.mapper;

import dev.ulisses.highperformanceapi.application.dto.response.NotificationResponse;
import dev.ulisses.highperformanceapi.domain.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "orderId", source = "order.id")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);

}
