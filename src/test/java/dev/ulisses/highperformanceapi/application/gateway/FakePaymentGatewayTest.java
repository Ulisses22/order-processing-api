package dev.ulisses.highperformanceapi.application.gateway;

import dev.ulisses.highperformanceapi.application.dto.request.PaymentGatewayRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentGatewayResponse;
import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FakePaymentGatewayTest {

    private final FakePaymentGateway paymentGateway = new FakePaymentGateway();

    @Test
    void shouldProcessPaymentSuccessfully() {

        // Arrange

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentGatewayRequest request =
                new PaymentGatewayRequest(
                        paymentId,
                        orderId,
                        new BigDecimal("150.00"),
                        PaymentMethod.CREDIT_CARD
                );

        // Act

        PaymentGatewayResponse response = paymentGateway.process(request);

        // Assert

        assertNotNull(response);

        assertTrue(response.successful());

        assertNotNull(response.transactionId());
        assertFalse(response.transactionId().isBlank());

        assertNotNull(response.authorizationCode());
        assertFalse(response.authorizationCode().isBlank());

        assertNull(response.failureReason());
    }

    @Test
    void shouldReturnFailedPayment() {

        // Arrange

        FakePaymentGateway paymentGateway =
                new FakePaymentGateway(true);

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentGatewayRequest request =
                new PaymentGatewayRequest(
                        paymentId,
                        orderId,
                        new BigDecimal("150.00"),
                        PaymentMethod.CREDIT_CARD
                );

        // Act

        PaymentGatewayResponse response =
                paymentGateway.process(request);

        // Assert

        assertNotNull(response);

        assertFalse(response.successful());

        assertNull(response.transactionId());

        assertNull(response.authorizationCode());

        assertEquals(
                "Payment declined by fake gateway",
                response.failureReason()
        );
    }
}