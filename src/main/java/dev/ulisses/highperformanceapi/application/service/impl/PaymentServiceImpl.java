package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.dto.request.CreatePaymentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.application.exception.BusinessException;
import dev.ulisses.highperformanceapi.application.exception.DuplicateResourceException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.PaymentMapper;
import dev.ulisses.highperformanceapi.application.service.PaymentService;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.entity.Payment;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import dev.ulisses.highperformanceapi.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            PaymentMapper paymentMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResponse create(CreatePaymentRequest request) {

        Order order = getOrder(request.orderId());

        validateOrderCanBePaid(order);

        validatePaymentDoesNotExist(order);

        Payment payment = paymentMapper.toEntity(request);

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }

    private Order getOrder(UUID orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));
    }

    private void validateOrderCanBePaid(Order order) {

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Payment cannot be created because order status is "
                            + order.getStatus() + "."
            );
        }
    }

    private void validatePaymentDoesNotExist(Order order) {

        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new DuplicateResourceException(
                    "Payment already exists for order: " + order.getId()
            );
        }
    }
}
