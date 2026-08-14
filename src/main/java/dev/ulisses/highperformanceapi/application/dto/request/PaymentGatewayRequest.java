package dev.ulisses.highperformanceapi.application.dto.request;

import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentGatewayRequest(

        UUID paymentId,

        UUID orderId,

        BigDecimal amount,

        PaymentMethod paymentMethod

) {
}
