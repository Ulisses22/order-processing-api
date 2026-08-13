package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreateOrderRequest;
import dev.ulisses.highperformanceapi.application.dto.request.OrderItemRequest;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.application.event.OrderCreatedEvent;
import dev.ulisses.highperformanceapi.application.exception.BusinessException;
import dev.ulisses.highperformanceapi.application.exception.InsufficientStockException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.OrderMapper;
import dev.ulisses.highperformanceapi.application.service.impl.OrderServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.entity.OrderItem;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import dev.ulisses.highperformanceapi.domain.repository.OrderItemRepository;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;



@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;


    // CREATE ORDER TESTS
    @Test
    void shouldCreateOrderSuccessfully() {

        // Arrange

        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                List.of(new OrderItemRequest(productId, 2))
        );

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setStatus(CustomerStatus.ACTIVE);

        Product product = new Product();
        product.setId(productId);
        product.setStatus(ProductStatus.ACTIVE);
        product.setPrice(BigDecimal.valueOf(25));

        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setCustomer(customer);
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setTotalAmount(BigDecimal.valueOf(50));

        OrderResponse response = mock(OrderResponse.class);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(response);

        // Act

        OrderResponse result = orderService.create(request);

        // Assert

        assertNotNull(result);

        verify(customerRepository).findById(customerId);
        verify(productRepository).findById(productId);

        verify(inventoryService)
                .reserveStock(productId, 2);

        verify(orderRepository)
                .save(any(Order.class));

        verify(eventPublisher)
                .publishEvent(any(OrderCreatedEvent.class));

        verify(orderMapper)
                .toResponse(savedOrder);
    }

    @Test
    void shouldThrowWhenCustomerDoesNotExist() {

        // Arrange

        UUID customerId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                List.of()
        );

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.create(request)
        );

        verify(customerRepository).findById(customerId);

        verifyNoInteractions(
                productRepository,
                inventoryService,
                orderRepository,
                orderMapper,
                eventPublisher
        );
    }

    @Test
    void shouldThrowWhenCustomerIsInactive() {

        // Arrange

        UUID customerId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                List.of()
        );

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setStatus(CustomerStatus.INACTIVE);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        // Act & Assert

        assertThrows(
                BusinessException.class,
                () -> orderService.create(request)
        );

        verify(customerRepository).findById(customerId);

        verifyNoInteractions(
                productRepository,
                inventoryService,
                orderRepository,
                orderMapper,
                eventPublisher
        );
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {

        // Arrange

        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                List.of(new OrderItemRequest(productId, 2))
        );

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.create(request)
        );

        verify(customerRepository).findById(customerId);
        verify(productRepository).findById(productId);

        verifyNoInteractions(
                inventoryService,
                orderRepository,
                orderMapper,
                eventPublisher
        );
    }

    @Test
    void shouldThrowWhenProductIsInactive() {

        // Arrange

        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                List.of(new OrderItemRequest(productId, 2))
        );

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setStatus(CustomerStatus.ACTIVE);

        Product product = new Product();
        product.setId(productId);
        product.setStatus(ProductStatus.INACTIVE);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act & Assert

        assertThrows(
                BusinessException.class,
                () -> orderService.create(request)
        );

        verify(customerRepository).findById(customerId);
        verify(productRepository).findById(productId);

        verifyNoInteractions(
                inventoryService,
                orderRepository,
                orderMapper,
                eventPublisher
        );
    }

    @Test
    void shouldThrowWhenInventoryReservationFails() {

        // Arrange

        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                List.of(new OrderItemRequest(productId, 2))
        );

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setStatus(CustomerStatus.ACTIVE);

        Product product = new Product();
        product.setId(productId);
        product.setStatus(ProductStatus.ACTIVE);
        product.setPrice(BigDecimal.valueOf(25));

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        doThrow(new InsufficientStockException("Insufficient stock"))
                .when(inventoryService)
                .reserveStock(productId, 2);

        // Act & Assert

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.create(request)
        );

        verify(inventoryService)
                .reserveStock(productId, 2);

        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any());
        verify(orderMapper, never()).toResponse(any());
    }

    @Test
    void shouldFindOrderById() {

        // Arrange

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber("ORDER-001");

        OrderResponse response = mock(OrderResponse.class);

        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        // Act

        OrderResponse result = orderService.findById(order.getId());

        // Assert

        assertSame(response, result);

        verify(orderRepository).findById(order.getId());
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.findById(orderId)
        );

        assertEquals(
                "Order not found with id: " + orderId,
                exception.getMessage()
        );

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldReturnPagedOrders() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        Order order = new Order();
        order.setOrderNumber("ORDER-001");
        order.setStatus(OrderStatus.PENDING);

        OrderResponse response = mock(OrderResponse.class);

        Page<Order> orderPage = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(pageable))
                .thenReturn(orderPage);

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        // Act

        Page<OrderResponse> result = orderService.findAll(pageable);

        // Assert

        assertEquals(1, result.getTotalElements());
        assertEquals(response, result.getContent().getFirst());

        verify(orderRepository).findAll(pageable);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldReturnEmptyPageWhenNoOrdersExist() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        // Act

        Page<OrderResponse> result = orderService.findAll(pageable);

        // Assert

        assertTrue(result.isEmpty());

        verify(orderRepository).findAll(pageable);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void shouldCancelOrderSuccessfully() {

        // Arrange

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);

        Product product = new Product();
        product.setId(productId);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);

        order.addItem(orderItem);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        OrderResponse response = mock(OrderResponse.class);
        when(orderMapper.toResponse(order)).thenReturn(response);

        // Act

        OrderResponse result = orderService.cancel(orderId);

        // Assert

        assertAll(
                () -> assertEquals(OrderStatus.CANCELLED, order.getStatus()),
                () -> assertEquals(response, result)
        );

        verify(inventoryService).releaseStock(productId, 2);
        verify(orderRepository).save(order);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void shouldThrowWhenOrderToCancelDoesNotExist() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act & Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.cancel(orderId)
        );

        assertEquals(
                "Order not found with id: " + orderId,
                exception.getMessage()
        );

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(inventoryService);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrderIsAlreadyCancelled() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act & Assert

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancel(orderId)
        );

        assertEquals(
                "Order cannot be cancelled because its status is CANCELLED.",
                exception.getMessage()
        );

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(inventoryService);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrderIsNotPending() {

        // Arrange

        UUID orderId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act & Assert

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancel(orderId)
        );

        assertEquals(
                "Order cannot be cancelled because its status is SHIPPED.",
                exception.getMessage()
        );

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(inventoryService);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldReleaseReservedInventoryWhenOrderIsCancelled() {

        // Arrange

        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);

        Product product = new Product();
        product.setId(productId);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(3);

        order.addItem(orderItem);

        OrderResponse response = mock(OrderResponse.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        // Act

        orderService.cancel(orderId);

        // Assert

        verify(orderRepository).save(order);
    }

}
