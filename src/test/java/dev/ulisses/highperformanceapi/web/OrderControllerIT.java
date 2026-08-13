package dev.ulisses.highperformanceapi.web;


import dev.ulisses.highperformanceapi.application.dto.request.CreateOrderRequest;
import dev.ulisses.highperformanceapi.application.dto.request.OrderItemRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateOrderStatusRequest;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Inventory;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import dev.ulisses.highperformanceapi.domain.repository.InventoryRepository;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.containsString;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${APP_SECURITY_USERNAME}")
    private String username;

    @Value("${APP_SECURITY_PASSWORD}")
    private String password;


    // HELPERS
    private Customer activeCustomer() {

        Customer customer = new Customer();

        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setStatus(CustomerStatus.ACTIVE);

        return customer;
    }

    private Product activeProduct() {

        Product product = new Product();

        product.setName("Mechanical Keyboard");
        product.setDescription("Gaming keyboard");
        product.setSku("KEYBOARD-001");
        product.setPrice(BigDecimal.valueOf(25.00));
        product.setStatus(ProductStatus.ACTIVE);

        return product;
    }

    private Inventory inventory(Product product, int quantity) {

        Inventory inventory = new Inventory();

        inventory.setProduct(product);
        inventory.setAvailableQuantity(quantity);
        inventory.setReservedQuantity(0);

        return inventory;
    }


    // TESTS
    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        // Act & Assert

        mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customer.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalAmount").value(50.00));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(UUID.randomUUID(), 2)
                )
        );

        // Act & Assert

        mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Product not found")))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"));
    }

    @Test
    void shouldReturn422WhenStockIsInsufficient() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 1)
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        // Act & Assert

        mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Insufficient inventory."))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {

        // Arrange

        String request = """
        {
          "customerId": null,
          "items": []
        }
        """;

        // Act & Assert

        mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.errors.customerId").value("Customer id is required."))
                .andExpect(jsonPath("$.errors.items").value("Order must contain at least one item."));
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() throws Exception {

        // Arrange

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(inventory(product, 100));

        CreateOrderRequest request = new CreateOrderRequest(
                UUID.randomUUID(),
                List.of(new OrderItemRequest(product.getId(), 2))
        );

        // Act & Assert

        mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("Customer not found")))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"));
    }

    @Test
    void shouldReturnOrderById() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest createRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(response, OrderResponse.class);

        // Act & Assert

        mockMvc.perform(get("/api/v1/orders/{id}", createdOrder.id())
                        .with(httpBasic(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrder.id().toString()))
                .andExpect(jsonPath("$.customerId").value(customer.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalAmount").value(50.00));
    }

    @Test
    void shouldReturn404WhenOrderDoesNotExist() throws Exception {

        // Arrange

        UUID orderId = UUID.randomUUID();

        // Act & Assert

        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .with(httpBasic(username, password)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("Order not found")))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/" + orderId));
    }

    @Test
    void shouldReturnPagedOrders() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                )
        );

        mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Act & Assert

        mockMvc.perform(get("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].customerId").value(customer.getId().toString()))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldReturnEmptyPage() throws Exception {

        // Act & Assert

        mockMvc.perform(get("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void shouldCancelOrderSuccessfully() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 2))
        );

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder =
                objectMapper.readValue(response, OrderResponse.class);

        // Act & Assert

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", createdOrder.id())
                        .with(httpBasic(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrder.id().toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturn404WhenOrderToCancelDoesNotExist() throws Exception {

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", UUID.randomUUID())
                        .with(httpBasic(username, password)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("Order not found")));
    }

    @Test
    void shouldReturn422WhenOrderIsAlreadyCancelled() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(response, OrderResponse.class);

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", order.id())
                        .with(httpBasic(username, password)))
                .andExpect(status().isOk());

        // Act & Assert

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", order.id())
                        .with(httpBasic(username, password)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message",
                        containsString("CANCELLED")));
    }

    @Test
    void shouldReturn422WhenOrderIsNotPending() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1))
        );

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse created =
                objectMapper.readValue(response, OrderResponse.class);

        Order order = orderRepository.findById(created.id()).orElseThrow();
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        // Act & Assert

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", created.id())
                        .with(httpBasic(username, password)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message",
                        containsString("SHIPPED")));
    }

    @Test
    void shouldUpdateOrderStatusToProcessing() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest createRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                )
        );

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order = objectMapper.readValue(response, OrderResponse.class);

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);

        // Act & Assert

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.id())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void shouldUpdateOrderStatusToShipped() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest createRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                )
        );

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order = objectMapper.readValue(response, OrderResponse.class);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.id())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.PROCESSING))))
                .andExpect(status().isOk());

        // Act & Assert

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.id())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.SHIPPED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void shouldUpdateOrderStatusToDelivered() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest createRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                )
        );

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order = objectMapper.readValue(response, OrderResponse.class);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.id())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.PROCESSING))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.id())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.SHIPPED))))
                .andExpect(status().isOk());

        // Act & Assert

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.id())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.DELIVERED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void shouldReturn404WhenOrderToUpdateDoesNotExist() throws Exception {

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(OrderStatus.PROCESSING);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", UUID.randomUUID())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("Order not found")));
    }

    @Test
    void shouldReturn422WhenStatusTransitionIsInvalid() throws Exception {

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest createRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                )
        );

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order = objectMapper.readValue(response, OrderResponse.class);

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest(OrderStatus.DELIVERED);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.id())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }
}
