package dev.ulisses.highperformanceapi.application.dto.request;

import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull(message = "Order id is required.")
        UUID orderId,

        @NotNull(message = "Payment method is required.")
        PaymentMethod paymentMethod

) {
}
