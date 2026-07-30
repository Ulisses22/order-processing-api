package dev.ulisses.highperformanceapi.application.mapper;

import dev.ulisses.highperformanceapi.application.dto.response.OrderItemResponse;
import dev.ulisses.highperformanceapi.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    OrderItemResponse toResponse(OrderItem orderItem);

}