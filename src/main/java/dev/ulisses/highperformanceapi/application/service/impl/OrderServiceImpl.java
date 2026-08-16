package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.dto.request.CreateOrderRequest;
import dev.ulisses.highperformanceapi.application.dto.request.OrderItemRequest;
import dev.ulisses.highperformanceapi.application.dto.request.OrderSearchRequest;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.application.event.OrderCreatedEvent;
import dev.ulisses.highperformanceapi.application.exception.BusinessException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.OrderMapper;
import dev.ulisses.highperformanceapi.application.service.InventoryService;
import dev.ulisses.highperformanceapi.application.service.OrderService;
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
import dev.ulisses.highperformanceapi.domain.specification.OrderSpecification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    private final InventoryService inventoryService;

    private final OrderMapper orderMapper;

    private final ApplicationEventPublisher eventPublisher;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            InventoryService inventoryService,
            OrderMapper orderMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderResponse create(CreateOrderRequest request) {

        Customer customer = getCustomer(request.customerId());

        List<Product> products = getProducts(request.items());

        BigDecimal totalAmount = calculateTotal(products, request.items());

        reserveInventory(products, request.items());

        Order order = buildOrder(customer, totalAmount, request.shippingAddress());

        List<OrderItem> orderItems = buildOrderItems(order, products, request.items());

        orderItems.forEach(order::addItem);

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId()));

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return orderMapper.toResponse(order);
    }

    @Override
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    @Override
    public OrderResponse cancel(UUID id) {

        Order order = getOrder(id);

        validateOrderCanBeCancelled(order);

        releaseReservedInventory(order);

        order.setStatus(OrderStatus.CANCELLED);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateStatus(UUID id, OrderStatus newStatus) {

        Order order = getOrder(id);

        validateStatusTransition(order, newStatus);

        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toResponse(updatedOrder);
    }

    // Helpers

    private Customer getCustomer(UUID customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId
                ));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(
                    "Customer is not active: " + customerId
            );
        }

        return customer;
    }

    private List<Product> getProducts(List<OrderItemRequest> items) {

        List<Product> products = new ArrayList<>();

        for (OrderItemRequest item : items) {

            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + item.productId()
                    ));

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BusinessException(
                        "Product is not active: " + product.getId()
                );
            }

            products.add(product);
        }

        return products;
    }

    private BigDecimal calculateTotal(
            List<Product> products,
            List<OrderItemRequest> items
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < items.size(); i++) {

            Product product = products.get(i);
            OrderItemRequest item = items.get(i);

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()));

            total = total.add(subtotal);
        }

        return total;
    }

    private void reserveInventory(
            List<Product> products,
            List<OrderItemRequest> items
    ) {

        for (int i = 0; i < items.size(); i++) {

            Product product = products.get(i);
            OrderItemRequest item = items.get(i);

            inventoryService.reserveStock(
                    product.getId(),
                    item.quantity()
            );
        }
    }

    private Order buildOrder(
            Customer customer,
            BigDecimal totalAmount,
            String shippingAddress
    ) {

        Order order = new Order();

        order.setCustomer(customer);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber(generateOrderNumber());

        return order;
    }

    // TODO: make it utils class
    private String generateOrderNumber() {
        return UUID.randomUUID().toString();
    }


    private List<OrderItem> buildOrderItems(
            Order order,
            List<Product> products,
            List<OrderItemRequest> items
    ) {

        List<OrderItem> orderItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {

            Product product = products.get(i);
            OrderItemRequest item = items.get(i);

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);

            orderItem.setProductSku(product.getSku());
            orderItem.setProductName(product.getName());

            orderItem.setQuantity(item.quantity());
            orderItem.setUnitPrice(product.getPrice());

            orderItem.setSubtotal(
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(item.quantity()))
            );

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private void releaseReservedInventory(Order order) {

        for (OrderItem item : order.getItems()) {

            inventoryService.releaseStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }
    }

    private void validateOrderCanBeCancelled(Order order) {

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Order cannot be cancelled because its status is "
                            + order.getStatus() + "."
            );
        }
    }

    private Order getOrder(UUID orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));
    }

    private void validateStatusTransition(
            Order order,
            OrderStatus newStatus
    ) {

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            throw new BusinessException(
                    "Order is already in status: " + currentStatus
            );
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(
                    "Cancelled orders cannot be updated."
            );
        }

        if (currentStatus == OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Delivered orders cannot be updated."
            );
        }

        boolean validTransition = switch (currentStatus) {

            case PENDING ->
                    newStatus == OrderStatus.PROCESSING
                            || newStatus == OrderStatus.CANCELLED;

            case PROCESSING ->
                    newStatus == OrderStatus.SHIPPED
                            || newStatus == OrderStatus.CANCELLED;

            case SHIPPED ->
                    newStatus == OrderStatus.DELIVERED;

            default -> false;
        };

        if (!validTransition) {
            throw new BusinessException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> search(OrderSearchRequest request, Pageable pageable) {

        return orderRepository.findAll(OrderSpecification.withFilters(request), pageable).map(orderMapper::toResponse);
    }
}
