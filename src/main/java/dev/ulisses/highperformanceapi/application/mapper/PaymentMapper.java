package dev.ulisses.highperformanceapi.application.mapper;

import dev.ulisses.highperformanceapi.application.dto.request.CreatePaymentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.domain.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "method", source = "paymentMethod")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "authorizationCode", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    Payment toEntity(CreatePaymentRequest request);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "paymentMethod", source = "method")
    PaymentResponse toResponse(Payment payment);

}
