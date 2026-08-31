package dev.ulisses.highperformanceapi.domain.repository;

import dev.ulisses.highperformanceapi.application.dto.request.OrderSearchRequest;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.specification.OrderSpecification;
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
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(OrderRepositoryIT.TestCacheConfiguration.class)
class OrderRepositoryIT {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @TestConfiguration
    static class TestCacheConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Test
    void shouldFindOrderByOrderNumber() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        customerRepository.saveAndFlush(customer);

        Order order = createOrder(
                customer,
                "ORD-001",
                OrderStatus.PENDING
        );

        orderRepository.saveAndFlush(order);

        Optional<Order> result =
                orderRepository.findByOrderNumber("ORD-001");

        assertThat(result)
                .isPresent()
                .get()
                .extracting(Order::getOrderNumber)
                .isEqualTo("ORD-001");
    }

    @Test
    void shouldReturnTrueWhenOrderNumberExists() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        customerRepository.saveAndFlush(customer);

        Order order = createOrder(
                customer,
                "ORD-002",
                OrderStatus.PENDING
        );

        orderRepository.saveAndFlush(order);

        assertThat(
                orderRepository.existsByOrderNumber("ORD-002")
        ).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrderNumberDoesNotExist() {
        assertThat(
                orderRepository.existsByOrderNumber("UNKNOWN-ORDER")
        ).isFalse();
    }

    @Test
    void shouldFindOrdersUsingCombinedSpecificationFilters() {
        Customer matchingCustomer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        Customer anotherCustomer = createCustomer(
                "Jane",
                "Smith",
                "jane@example.com"
        );

        customerRepository.save(matchingCustomer);
        customerRepository.saveAndFlush(anotherCustomer);

        Order matchingOrder = createOrder(
                matchingCustomer,
                "ORD-003",
                OrderStatus.PAID
        );

        Order differentCustomerOrder = createOrder(
                anotherCustomer,
                "ORD-OTHER",
                OrderStatus.PAID
        );

        Order differentStatusOrder = createOrder(
                matchingCustomer,
                "ORD-004",
                OrderStatus.CANCELLED
        );

        orderRepository.save(matchingOrder);
        orderRepository.save(differentCustomerOrder);
        orderRepository.saveAndFlush(differentStatusOrder);

        Instant createdFrom = matchingOrder.getCreatedAt().minusSeconds(1);
        Instant createdTo = matchingOrder.getCreatedAt().plusSeconds(1);

        OrderSearchRequest request = new OrderSearchRequest(
                matchingCustomer.getId(),
                OrderStatus.PAID,
                "ORD-003",
                createdFrom,
                createdTo
        );

        var result = orderRepository.findAll(
                OrderSpecification.withFilters(request)
        );

        assertThat(result)
                .hasSize(1)
                .extracting(Order::getOrderNumber)
                .containsExactly("ORD-003");

        assertThat(result.getFirst().getCustomer().getId())
                .isEqualTo(matchingCustomer.getId());

        assertThat(result.getFirst().getStatus())
                .isEqualTo(OrderStatus.PAID);
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
            String orderNumber,
            OrderStatus status
    ) {
        Order order = new Order();

        order.setCustomer(customer);
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        order.setShippingAddress("123 Main Street");
        order.setStatus(status);
        order.setOrderNumber(orderNumber);

        return order;
    }
}
