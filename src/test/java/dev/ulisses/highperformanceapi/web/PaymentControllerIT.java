package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.*;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Inventory;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.*;
import dev.ulisses.highperformanceapi.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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
public class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

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
    void shouldCreatePayment() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(
                orderResponse,
                OrderResponse.class
        );

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                createdOrder.id(),
                PaymentMethod.CREDIT_CARD
        );

        // Act & Assert

        mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId")
                        .value(createdOrder.id().toString()))
                .andExpect(jsonPath("$.amount")
                        .value(50.00))
                .andExpect(jsonPath("$.paymentMethod")
                        .value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));
    }

    @Test
    void shouldReturn404WhenOrderDoesNotExist() throws Exception {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                orderId,
                PaymentMethod.CREDIT_CARD
        );

        // Act & Assert

        mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Order not found with id: " + orderId))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/payments"));
    }

    @Test
    void shouldReturn422WhenOrderIsNotPending() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(inventory(product, 100));

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 2))
        );

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(
                orderResponse,
                OrderResponse.class
        );

        UpdateOrderStatusRequest statusRequest = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", createdOrder.id())
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk());

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                createdOrder.id(),
                PaymentMethod.CREDIT_CARD
        );

        // Act & Assert

        mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Payment cannot be created because order status is PROCESSING."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/payments"));
    }

    @Test
    void shouldReturn409WhenPaymentAlreadyExists() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(inventory(product, 100));

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 2))
        );

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(
                orderResponse,
                OrderResponse.class
        );

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                createdOrder.id(),
                PaymentMethod.CREDIT_CARD
        );

        // Create first payment

        mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - attempt duplicate payment

        mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Payment already exists for order: " + createdOrder.id()))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/payments"));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {

        // Arrange

        String request = """
    {
      "orderId": null,
      "paymentMethod": null
    }
    """;

        // Act & Assert

        mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/payments"))
                .andExpect(jsonPath("$.errors.orderId")
                        .value("Order id is required."))
                .andExpect(jsonPath("$.errors.paymentMethod")
                        .value("Payment method is required."));
    }

    @Test
    void shouldAuthorizePayment() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(
                orderResponse,
                OrderResponse.class
        );

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                createdOrder.id(),
                PaymentMethod.CREDIT_CARD
        );

        String paymentResponse = mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentResponse createdPayment = objectMapper.readValue(
                paymentResponse,
                PaymentResponse.class
        );

        UpdatePaymentStatusRequest statusRequest =
                new UpdatePaymentStatusRequest(PaymentStatus.AUTHORIZED);

        // Act & Assert

        mockMvc.perform(patch(
                        "/api/v1/payments/{id}/status",
                        createdPayment.id()
                )
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(createdPayment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(createdOrder.id().toString()))
                .andExpect(jsonPath("$.amount")
                        .value(50.00))
                .andExpect(jsonPath("$.paymentMethod")
                        .value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status")
                        .value("AUTHORIZED"));
    }

    @Test
    void shouldFailPayment() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(
                orderResponse,
                OrderResponse.class
        );

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                createdOrder.id(),
                PaymentMethod.CREDIT_CARD
        );

        String paymentResponse = mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentResponse createdPayment = objectMapper.readValue(
                paymentResponse,
                PaymentResponse.class
        );

        UpdatePaymentStatusRequest statusRequest =
                new UpdatePaymentStatusRequest(PaymentStatus.FAILED);

        // Act & Assert

        mockMvc.perform(patch(
                        "/api/v1/payments/{id}/status",
                        createdPayment.id()
                )
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(createdPayment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(createdOrder.id().toString()))
                .andExpect(jsonPath("$.amount")
                        .value(50.00))
                .andExpect(jsonPath("$.paymentMethod")
                        .value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status")
                        .value("FAILED"));
    }

    @Test
    void shouldRejectInvalidPaymentStatusTransition() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(
                orderResponse,
                OrderResponse.class
        );

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                createdOrder.id(),
                PaymentMethod.CREDIT_CARD
        );

        String paymentResponse = mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentResponse createdPayment = objectMapper.readValue(
                paymentResponse,
                PaymentResponse.class
        );

        // First transition: PENDING → AUTHORIZED

        UpdatePaymentStatusRequest authorizeRequest =
                new UpdatePaymentStatusRequest(PaymentStatus.AUTHORIZED);

        mockMvc.perform(patch(
                        "/api/v1/payments/{id}/status",
                        createdPayment.id()
                )
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));

        // Act & Assert: AUTHORIZED → FAILED

        UpdatePaymentStatusRequest failRequest =
                new UpdatePaymentStatusRequest(PaymentStatus.FAILED);

        mockMvc.perform(patch(
                        "/api/v1/payments/{id}/status",
                        createdPayment.id()
                )
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid payment status transition from AUTHORIZED to FAILED"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/payments/" + createdPayment.id() + "/status"));
    }

    @Test
    void shouldRejectPaymentWhenAlreadyInSameStatus() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(
                orderResponse,
                OrderResponse.class
        );

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                createdOrder.id(),
                PaymentMethod.CREDIT_CARD
        );

        String paymentResponse = mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentResponse createdPayment = objectMapper.readValue(
                paymentResponse,
                PaymentResponse.class
        );

        // First transition: PENDING → AUTHORIZED

        UpdatePaymentStatusRequest authorizeRequest =
                new UpdatePaymentStatusRequest(PaymentStatus.AUTHORIZED);

        mockMvc.perform(patch(
                        "/api/v1/payments/{id}/status",
                        createdPayment.id()
                )
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));

        // Act & Assert: AUTHORIZED → AUTHORIZED

        mockMvc.perform(patch(
                        "/api/v1/payments/{id}/status",
                        createdPayment.id()
                )
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizeRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Payment is already in status: AUTHORIZED"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/payments/" + createdPayment.id() + "/status"));
    }

    @Test
    void shouldReturn404WhenPaymentDoesNotExist() throws Exception {

        // Arrange

        UUID paymentId = UUID.randomUUID();

        UpdatePaymentStatusRequest request =
                new UpdatePaymentStatusRequest(PaymentStatus.AUTHORIZED);

        // Act & Assert

        mockMvc.perform(patch(
                        "/api/v1/payments/{id}/status",
                        paymentId
                )
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Payment not found with id: " + paymentId))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/payments/" + paymentId + "/status"));
    }

    @Test
    void shouldMoveOrderToProcessingWhenPaymentIsAuthorized() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                )
        );

        String orderResponse = mockMvc.perform(post("/api/v1/orders")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse createdOrder = objectMapper.readValue(
                orderResponse,
                OrderResponse.class
        );

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                createdOrder.id(),
                PaymentMethod.CREDIT_CARD
        );

        String paymentResponse = mockMvc.perform(post("/api/v1/payments")
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentResponse createdPayment = objectMapper.readValue(
                paymentResponse,
                PaymentResponse.class
        );

        UpdatePaymentStatusRequest statusRequest =
                new UpdatePaymentStatusRequest(PaymentStatus.AUTHORIZED);

        // Act

        mockMvc.perform(patch(
                        "/api/v1/payments/{id}/status",
                        createdPayment.id()
                )
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));

        // Assert

        mockMvc.perform(get(
                        "/api/v1/orders/{id}",
                        createdOrder.id()
                )
                        .with(httpBasic(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(createdOrder.id().toString()))
                .andExpect(jsonPath("$.status")
                        .value("PROCESSING"));
    }

}
