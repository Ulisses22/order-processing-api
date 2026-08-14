package dev.ulisses.highperformanceapi.application.event.listener;

import dev.ulisses.highperformanceapi.application.dto.request.UpdatePaymentStatusRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.application.event.PaymentAuthorizedEvent;
import dev.ulisses.highperformanceapi.application.service.OrderService;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.ulisses.highperformanceapi.application.dto.request.CreateOrderRequest;
import dev.ulisses.highperformanceapi.application.dto.request.OrderItemRequest;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Product;



import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentEventListener paymentEventListener;

    @Value("${APP_SECURITY_USERNAME}")
    private String username;

    @Value("${APP_SECURITY_PASSWORD}")
    private String password;

    @Test
    void shouldMoveOrderToProcessingWhenPaymentIsAuthorized() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentAuthorizedEvent event = new PaymentAuthorizedEvent(
                paymentId,
                orderId
        );

        paymentEventListener.handlePaymentAuthorized(event);

        verify(orderService).updateStatus(
                orderId,
                OrderStatus.PROCESSING
        );
    }
}
