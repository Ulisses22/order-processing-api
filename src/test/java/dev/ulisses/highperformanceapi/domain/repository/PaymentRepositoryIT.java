package dev.ulisses.highperformanceapi.domain.repository;

import dev.ulisses.highperformanceapi.application.dto.request.PaymentSearchRequest;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.entity.Payment;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;
import dev.ulisses.highperformanceapi.domain.specification.PaymentSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(PaymentRepositoryIT.TestCacheConfiguration.class)
class PaymentRepositoryIT {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @TestConfiguration
    static class TestCacheConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Test
    void shouldFindPaymentByOrderId() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        customerRepository.saveAndFlush(customer);

        Order order = createOrder(
                customer,
                "ORD-PAY-001"
        );

        orderRepository.saveAndFlush(order);

        Payment payment = createPayment(
                order,
                PaymentStatus.PAID,
                PaymentMethod.CREDIT_CARD,
                "TX-001"
        );

        paymentRepository.saveAndFlush(payment);

        Optional<Payment> result =
                paymentRepository.findByOrderId(order.getId());

        assertThat(result)
                .isPresent()
                .get()
                .extracting(Payment::getTransactionId)
                .isEqualTo("TX-001");
    }

    @Test
    void shouldReturnTrueWhenPaymentExistsForOrder() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        customerRepository.saveAndFlush(customer);

        Order order = createOrder(
                customer,
                "ORD-PAY-002"
        );

        orderRepository.saveAndFlush(order);

        Payment payment = createPayment(
                order,
                PaymentStatus.PENDING,
                PaymentMethod.PAYPAL,
                "TX-002"
        );

        paymentRepository.saveAndFlush(payment);

        assertThat(
                paymentRepository.existsByOrderId(order.getId())
        ).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPaymentDoesNotExistForOrder() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        customerRepository.saveAndFlush(customer);

        Order order = createOrder(
                customer,
                "ORD-PAY-003"
        );

        orderRepository.saveAndFlush(order);

        assertThat(
                paymentRepository.existsByOrderId(order.getId())
        ).isFalse();
    }

    @Test
    void shouldFindPaymentsUsingCombinedSpecificationFilters() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        customerRepository.saveAndFlush(customer);

        Order matchingOrder = createOrder(
                customer,
                "ORD-PAY-004"
        );

        Order differentOrder = createOrder(
                customer,
                "ORD-PAY-005"
        );

        orderRepository.save(matchingOrder);
        orderRepository.saveAndFlush(differentOrder);

        Payment matchingPayment = createPayment(
                matchingOrder,
                PaymentStatus.PAID,
                PaymentMethod.CREDIT_CARD,
                "TX-MATCH"
        );

        Payment differentStatusPayment = createPayment(
                matchingOrder,
                PaymentStatus.FAILED,
                PaymentMethod.CREDIT_CARD,
                "TX-FAILED"
        );

        Payment differentMethodPayment = createPayment(
                matchingOrder,
                PaymentStatus.PAID,
                PaymentMethod.PAYPAL,
                "TX-PAYPAL"
        );

        Payment differentOrderPayment = createPayment(
                differentOrder,
                PaymentStatus.PAID,
                PaymentMethod.CREDIT_CARD,
                "TX-OTHER"
        );

        paymentRepository.save(matchingPayment);
        paymentRepository.save(differentStatusPayment);
        paymentRepository.save(differentMethodPayment);
        paymentRepository.saveAndFlush(differentOrderPayment);

        PaymentSearchRequest request = new PaymentSearchRequest(
                matchingOrder.getId(),
                PaymentStatus.PAID,
                PaymentMethod.CREDIT_CARD,
                "TX-MATCH",
                matchingPayment.getCreatedAt().minusSeconds(1),
                matchingPayment.getCreatedAt().plusSeconds(1)
        );

        var result = paymentRepository.findAll(
                PaymentSpecification.withFilters(request)
        );

        assertThat(result)
                .hasSize(1)
                .extracting(Payment::getTransactionId)
                .containsExactly("TX-MATCH");

        assertThat(result.getFirst().getOrder().getId())
                .isEqualTo(matchingOrder.getId());

        assertThat(result.getFirst().getStatus())
                .isEqualTo(PaymentStatus.PAID);

        assertThat(result.getFirst().getMethod())
                .isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    private Customer createCustomer(
            String firstName,
            String lastName,
            String email
    ) {
        Customer customer = new Customer();

        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setStatus(CustomerStatus.ACTIVE);

        return customer;
    }

    private Order createOrder(
            Customer customer,
            String orderNumber
    ) {
        Order order = new Order();

        order.setCustomer(customer);
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        order.setShippingAddress("123 Main Street");
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber(orderNumber);

        return order;
    }

    private Payment createPayment(
            Order order,
            PaymentStatus status,
            PaymentMethod method,
            String transactionId
    ) {
        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setStatus(status);
        payment.setMethod(method);
        payment.setTransactionId(transactionId);

        return payment;
    }
}
