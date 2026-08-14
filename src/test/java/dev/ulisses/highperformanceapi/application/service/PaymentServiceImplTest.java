package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreatePaymentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.application.event.PaymentAuthorizedEvent;
import dev.ulisses.highperformanceapi.application.event.PaymentFailedEvent;
import dev.ulisses.highperformanceapi.application.exception.BusinessException;
import dev.ulisses.highperformanceapi.application.exception.DuplicateResourceException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.PaymentMapper;
import dev.ulisses.highperformanceapi.application.service.impl.PaymentServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.entity.Payment;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import dev.ulisses.highperformanceapi.domain.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldCreatePayment() {

        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                orderId,
                PaymentMethod.CREDIT_CARD
        );

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);

        PaymentResponse response = new PaymentResponse(
                paymentId,
                orderId,
                new BigDecimal("150.00"),
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.PENDING,
                null,
                null,
                null,
                null
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(orderId))
                .thenReturn(false);

        when(paymentMapper.toEntity(request))
                .thenReturn(payment);

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        PaymentResponse result = paymentServiceImpl.create(request);

        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(orderId, result.orderId());
        assertEquals(new BigDecimal("150.00"), result.amount());
        assertEquals(PaymentMethod.CREDIT_CARD, result.paymentMethod());
        assertEquals(PaymentStatus.PENDING, result.status());

        verify(orderRepository).findById(orderId);
        verify(paymentRepository).existsByOrderId(orderId);
        verify(paymentMapper).toEntity(request);
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    void shouldRejectPaymentWhenOrderDoesNotExist() {

        UUID orderId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                orderId,
                PaymentMethod.CREDIT_CARD
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentServiceImpl.create(request)
        );

        assertEquals(
                "Order not found with id: " + orderId,
                exception.getMessage()
        );

        verify(orderRepository).findById(orderId);
        verify(paymentRepository, never()).existsByOrderId(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentMapper, never()).toEntity(any());
    }

    @Test
    void shouldRejectPaymentWhenOrderIsNotPending() {

        UUID orderId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                orderId,
                PaymentMethod.CREDIT_CARD
        );

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentServiceImpl.create(request)
        );

        assertEquals(
                "Payment cannot be created because order status is PROCESSING.",
                exception.getMessage()
        );

        verify(orderRepository).findById(orderId);
        verify(paymentRepository, never()).existsByOrderId(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentMapper, never()).toEntity(any());
    }

    @Test
    void shouldRejectPaymentWhenPaymentAlreadyExists() {

        UUID orderId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                orderId,
                PaymentMethod.CREDIT_CARD
        );

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(orderId))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> paymentServiceImpl.create(request)
        );

        assertEquals(
                "Payment already exists for order: " + orderId,
                exception.getMessage()
        );

        verify(orderRepository).findById(orderId);
        verify(paymentRepository).existsByOrderId(orderId);
        verify(paymentRepository, never()).save(any());
        verify(paymentMapper, never()).toEntity(any());
    }

    @Test
    void shouldUseOrderTotalAsPaymentAmount() {

        UUID orderId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                orderId,
                PaymentMethod.CREDIT_CARD
        );

        BigDecimal orderTotal = new BigDecimal("250.00");

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(orderTotal);
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(orderId))
                .thenReturn(false);

        when(paymentMapper.toEntity(request))
                .thenReturn(payment);

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        PaymentResponse response = new PaymentResponse(
                UUID.randomUUID(),
                orderId,
                orderTotal,
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.PENDING,
                null,
                null,
                null,
                null
        );

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        paymentServiceImpl.create(request);

        assertEquals(orderTotal, payment.getAmount());

        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldAuthorizePayment() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);

        PaymentResponse response = new PaymentResponse(
                paymentId,
                orderId,
                new BigDecimal("150.00"),
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.AUTHORIZED,
                null,
                null,
                null,
                null
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        PaymentResponse result = paymentServiceImpl.updateStatus(
                paymentId,
                PaymentStatus.AUTHORIZED
        );

        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(PaymentStatus.AUTHORIZED, result.status());

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    void shouldFailPayment() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);

        PaymentResponse response = new PaymentResponse(
                paymentId,
                orderId,
                new BigDecimal("150.00"),
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.FAILED,
                null,
                null,
                null,
                null
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        PaymentResponse result = paymentServiceImpl.updateStatus(
                paymentId,
                PaymentStatus.FAILED
        );

        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(PaymentStatus.FAILED, result.status());

        assertEquals(PaymentStatus.FAILED, payment.getStatus());

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    void shouldRejectInvalidPaymentStatusTransition() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.AUTHORIZED);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentServiceImpl.updateStatus(
                        paymentId,
                        PaymentStatus.FAILED
                )
        );

        assertEquals(
                "Invalid payment status transition from AUTHORIZED to FAILED",
                exception.getMessage()
        );

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
        verify(paymentMapper, never()).toResponse(any());
    }

    @Test
    void shouldRejectPaymentWhenAlreadyInSameStatus() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.AUTHORIZED);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentServiceImpl.updateStatus(
                        paymentId,
                        PaymentStatus.AUTHORIZED
                )
        );

        assertEquals(
                "Payment is already in status: AUTHORIZED",
                exception.getMessage()
        );

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
        verify(paymentMapper, never()).toResponse(any());
    }

    @Test
    void shouldReturn404WhenPaymentDoesNotExist() {

        UUID paymentId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentServiceImpl.updateStatus(
                        paymentId,
                        PaymentStatus.AUTHORIZED
                )
        );

        assertEquals(
                "Payment not found with id: " + paymentId,
                exception.getMessage()
        );

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
        verify(paymentMapper, never()).toResponse(any());
    }

    @Test
    void shouldPublishPaymentAuthorizedEvent() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);

        PaymentResponse response = new PaymentResponse(
                paymentId,
                orderId,
                new BigDecimal("150.00"),
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.AUTHORIZED,
                null,
                null,
                null,
                null
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        PaymentResponse result = paymentServiceImpl.updateStatus(
                paymentId,
                PaymentStatus.AUTHORIZED
        );

        assertNotNull(result);
        assertEquals(PaymentStatus.AUTHORIZED, result.status());

        verify(paymentRepository).save(payment);

        verify(eventPublisher).publishEvent(
                new PaymentAuthorizedEvent(paymentId, orderId)
        );
    }

    @Test
    void shouldPublishPaymentFailedEvent() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);

        PaymentResponse response = new PaymentResponse(
                paymentId,
                orderId,
                new BigDecimal("150.00"),
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.FAILED,
                null,
                null,
                null,
                null
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        PaymentResponse result = paymentServiceImpl.updateStatus(
                paymentId,
                PaymentStatus.FAILED
        );

        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, result.status());

        verify(paymentRepository).save(payment);

        verify(eventPublisher).publishEvent(
                new PaymentFailedEvent(paymentId)
        );
    }

    @Test
    void shouldNotPublishPaymentEventForInvalidTransition() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.AUTHORIZED);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentServiceImpl.updateStatus(
                        paymentId,
                        PaymentStatus.FAILED
                )
        );

        assertEquals(
                "Invalid payment status transition from AUTHORIZED to FAILED",
                exception.getMessage()
        );

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(paymentMapper, never()).toResponse(any());
    }

    @Test
    void shouldNotPublishPaymentEventForSameStatus() {

        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.AUTHORIZED);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentServiceImpl.updateStatus(
                        paymentId,
                        PaymentStatus.AUTHORIZED
                )
        );

        assertEquals(
                "Payment is already in status: AUTHORIZED",
                exception.getMessage()
        );

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());

        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(paymentMapper, never()).toResponse(any());
    }
}
