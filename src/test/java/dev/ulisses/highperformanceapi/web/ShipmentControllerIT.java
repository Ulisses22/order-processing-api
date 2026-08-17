package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.CreateOrderRequest;
import dev.ulisses.highperformanceapi.application.dto.request.CreateShipmentRequest;
import dev.ulisses.highperformanceapi.application.dto.request.OrderItemRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateShipmentStatusRequest;
import dev.ulisses.highperformanceapi.application.dto.response.OrderResponse;
import dev.ulisses.highperformanceapi.application.dto.response.ShipmentResponse;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Inventory;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.enums.OrderStatus;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.enums.ShipmentStatus;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import dev.ulisses.highperformanceapi.domain.repository.InventoryRepository;
import dev.ulisses.highperformanceapi.domain.repository.OrderRepository;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShipmentControllerIT extends IntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

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

    @Test
    void shouldCreateShipmentSuccessfully() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                ),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        CreateShipmentRequest shipmentRequest =
                new CreateShipmentRequest(
                        order.id(),
                        "DHL"
                );

        // Act & Assert

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId")
                        .value(order.id().toString()))
                .andExpect(jsonPath("$.carrier")
                        .value("DHL"))
                .andExpect(jsonPath("$.trackingNumber")
                        .isString())
                .andExpect(jsonPath("$.trackingNumber")
                        .value(org.hamcrest.Matchers.startsWith("TRK-")))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));
    }

    @Test
    void shouldReturn404WhenOrderDoesNotExist() throws Exception {

        // Arrange

        UUID orderId = UUID.randomUUID();

        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                "DHL"
        );

        // Act & Assert

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString("Order not found")))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments"));
    }

    @Test
    void shouldReturn409WhenShipmentAlreadyExists() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                ),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        CreateShipmentRequest shipmentRequest =
                new CreateShipmentRequest(
                        order.id(),
                        "DHL"
                );

        // Create first shipment

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isCreated());

        // Act & Assert - try to create another shipment

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message",
                        org.hamcrest.Matchers.containsString(
                                "Shipment already exists for order"
                        )))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments"));
    }

    @Test
    void shouldReturn422WhenOrderIsCancelled() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                ),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        // Cancel the order

        mockMvc.perform(
                        patch("/api/v1/orders/{id}/cancel", order.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                )
                .andExpect(status().isOk());

        CreateShipmentRequest shipmentRequest =
                new CreateShipmentRequest(
                        order.id(),
                        "DHL"
                );

        // Act & Assert

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error")
                        .value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Shipment cannot be created for a cancelled order."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments"));
    }

    @Test
    void shouldReturn422WhenOrderIsRefunded() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                ),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        // Set order to REFUNDED directly for this test

        Order persistedOrder = orderRepository.findById(order.id())
                .orElseThrow();

        persistedOrder.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(persistedOrder);

        CreateShipmentRequest shipmentRequest =
                new CreateShipmentRequest(
                        order.id(),
                        "DHL"
                );

        // Act & Assert

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error")
                        .value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Shipment cannot be created for a refunded order."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments"));
    }

    @Test
    void shouldReturn422WhenOrderIsDelivered() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                ),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        // Set order to DELIVERED directly

        Order persistedOrder = orderRepository.findById(order.id())
                .orElseThrow();

        persistedOrder.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(persistedOrder);

        CreateShipmentRequest shipmentRequest =
                new CreateShipmentRequest(
                        order.id(),
                        "DHL"
                );

        // Act & Assert

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error")
                        .value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Shipment cannot be created for a delivered order."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments"));
    }

    @Test
    void shouldReturn400WhenShipmentRequestIsInvalid() throws Exception {

        // Arrange

        String request = """
    {
      "orderId": null,
      "carrier": ""
    }
    """;

        // Act & Assert

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments"))
                .andExpect(jsonPath("$.errors.orderId")
                        .value("Order id is required."))
                .andExpect(jsonPath("$.errors.carrier")
                        .value("Carrier is required."));
    }

    @Test
    void shouldReturnShipmentById() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                ),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        CreateShipmentRequest shipmentRequest =
                new CreateShipmentRequest(
                        order.id(),
                        "DHL"
                );

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        // Act & Assert

        mockMvc.perform(
                        get("/api/v1/shipments/{id}", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(order.id().toString()))
                .andExpect(jsonPath("$.carrier")
                        .value("DHL"))
                .andExpect(jsonPath("$.trackingNumber")
                        .value(shipment.trackingNumber()))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));
    }

    @Test
    void shouldReturn404WhenShipmentDoesNotExist() throws Exception {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        // Act & Assert

        mockMvc.perform(
                        get("/api/v1/shipments/{id}", shipmentId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Shipment not found with id: " + shipmentId))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments/" + shipmentId));
    }

    // PENDING > PREPARING
    @Test
    void shouldUpdateShipmentStatusToPreparing() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(product.getId(), 1)
                ),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        CreateShipmentRequest shipmentRequest =
                new CreateShipmentRequest(order.id(), "DHL");

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        UpdateShipmentStatusRequest statusRequest =
                new UpdateShipmentStatusRequest(
                        ShipmentStatus.PREPARING
                );

        // Act & Assert

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(order.id().toString()))
                .andExpect(jsonPath("$.carrier")
                        .value("DHL"))
                .andExpect(jsonPath("$.trackingNumber")
                        .value(shipment.trackingNumber()))
                .andExpect(jsonPath("$.status")
                        .value("PREPARING"));
    }

    // PREPARING > SHIPPED
    @Test
    void shouldUpdateShipmentStatusToShipped() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        CreateShipmentRequest shipmentRequest =
                new CreateShipmentRequest(order.id(), "DHL");

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(shipmentRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        // PENDING → PREPARING

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.PREPARING
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // Act & Assert: PREPARING → SHIPPED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.SHIPPED
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(order.id().toString()))
                .andExpect(jsonPath("$.carrier")
                        .value("DHL"))
                .andExpect(jsonPath("$.status")
                        .value("SHIPPED"));
    }

    // SHIPPED > IN_TRANSIT
    @Test
    void shouldUpdateShipmentStatusToInTransit() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateShipmentRequest(order.id(), "DHL")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        // PENDING → PREPARING

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.PREPARING
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // PREPARING → SHIPPED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.SHIPPED
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // Act & Assert: SHIPPED → IN_TRANSIT

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.IN_TRANSIT
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(order.id().toString()))
                .andExpect(jsonPath("$.carrier")
                        .value("DHL"))
                .andExpect(jsonPath("$.status")
                        .value("IN_TRANSIT"));
    }

    // IN_TRANSIT > DELIVERED
    @Test
    void shouldUpdateShipmentStatusToDelivered() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateShipmentRequest(order.id(), "DHL")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        // PENDING → PREPARING

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.PREPARING
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // PREPARING → SHIPPED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.SHIPPED
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // SHIPPED → IN_TRANSIT

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.IN_TRANSIT
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // Act & Assert: IN_TRANSIT → DELIVERED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.DELIVERED
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(order.id().toString()))
                .andExpect(jsonPath("$.carrier")
                        .value("DHL"))
                .andExpect(jsonPath("$.status")
                        .value("DELIVERED"));
    }

    // SHIPPED > RETURNED
    @Test
    void shouldUpdateShipmentStatusToReturned() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateShipmentRequest(order.id(), "DHL")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        // PENDING → PREPARING

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.PREPARING
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // PREPARING → SHIPPED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.SHIPPED
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // Act & Assert: SHIPPED → RETURNED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.RETURNED
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(order.id().toString()))
                .andExpect(jsonPath("$.carrier")
                        .value("DHL"))
                .andExpect(jsonPath("$.status")
                        .value("RETURNED"));
    }

    // IN_TRANSIT > RETURNED
    @Test
    void shouldUpdateShipmentStatusToReturnedFromInTransit() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateShipmentRequest(order.id(), "DHL")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        // PENDING → PREPARING

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.PREPARING
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // PREPARING → SHIPPED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.SHIPPED
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // SHIPPED → IN_TRANSIT

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.IN_TRANSIT
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // Act & Assert: IN_TRANSIT → RETURNED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.RETURNED
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipment.id().toString()))
                .andExpect(jsonPath("$.orderId")
                        .value(order.id().toString()))
                .andExpect(jsonPath("$.carrier")
                        .value("DHL"))
                .andExpect(jsonPath("$.status")
                        .value("RETURNED"));
    }

    // invalid transition
    @Test
    void shouldReturn422WhenShipmentStatusTransitionIsInvalid() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateShipmentRequest(order.id(), "DHL")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        UpdateShipmentStatusRequest statusRequest =
                new UpdateShipmentStatusRequest(
                        ShipmentStatus.DELIVERED
                );

        // Act & Assert

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusRequest))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error")
                        .value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid status transition from PENDING to DELIVERED"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments/" + shipment.id() + "/status"));
    }

    @Test
    void shouldReturn422WhenDeliveredShipmentIsUpdated() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateShipmentRequest(order.id(), "DHL")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        // Move shipment to DELIVERED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.PREPARING
                                        )
                                ))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.SHIPPED
                                        )
                                ))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.IN_TRANSIT
                                        )
                                ))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.DELIVERED
                                        )
                                ))
                )
                .andExpect(status().isOk());

        // Act & Assert

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.IN_TRANSIT
                                        )
                                ))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error")
                        .value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Delivered shipments cannot be updated."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments/" + shipment.id() + "/status"));
    }

    @Test
    void shouldReturn422WhenCancelledShipmentIsUpdated() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateShipmentRequest(order.id(), "DHL")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        // PENDING → CANCELLED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.CANCELLED
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Act & Assert: CANCELLED → SHIPPED

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new UpdateShipmentStatusRequest(
                                                ShipmentStatus.SHIPPED
                                        )
                                ))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error")
                        .value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Cancelled shipments cannot be updated."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments/" + shipment.id() + "/status"));
    }

    @Test
    void shouldReturn422WhenShipmentStatusIsAlreadyTheSame() throws Exception {

        // Arrange

        Customer customer = customerRepository.save(activeCustomer());

        Product product = productRepository.save(activeProduct());

        inventoryRepository.save(
                inventory(product, 100)
        );

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer.getId(),
                List.of(new OrderItemRequest(product.getId(), 1)),
                "221B Baker Street, London"
        );

        String orderResponse = mockMvc.perform(
                        post("/api/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse order =
                objectMapper.readValue(orderResponse, OrderResponse.class);

        String shipmentResponse = mockMvc.perform(
                        post("/api/v1/shipments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new CreateShipmentRequest(order.id(), "DHL")
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShipmentResponse shipment =
                objectMapper.readValue(shipmentResponse, ShipmentResponse.class);

        UpdateShipmentStatusRequest statusRequest =
                new UpdateShipmentStatusRequest(
                        ShipmentStatus.PENDING
                );

        // Act & Assert

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipment.id())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusRequest))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error")
                        .value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Shipment is already in status: PENDING"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments/" + shipment.id() + "/status"));
    }

    @Test
    void shouldReturn404WhenShipmentDoesNotExistDuringStatusUpdate() throws Exception {

        // Arrange

        UUID shipmentId = UUID.randomUUID();

        UpdateShipmentStatusRequest request =
                new UpdateShipmentStatusRequest(
                        ShipmentStatus.PREPARING
                );

        // Act & Assert

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", shipmentId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Shipment not found with id: " + shipmentId))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/shipments/" + shipmentId + "/status"));
    }

    @Test
    void shouldReturn400WhenShipmentStatusRequestIsInvalid() throws Exception {

        // Arrange

        String request = """
    {
      "status": null
    }
    """;

        // Act & Assert

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", UUID.randomUUID())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path")
                        .value(org.hamcrest.Matchers.containsString(
                                "/api/v1/shipments/"
                        )))
                .andExpect(jsonPath("$.errors.status")
                        .value("Shipment status is required."));
    }
}
