package dev.ulisses.highperformanceapi.web;


import dev.ulisses.highperformanceapi.application.dto.request.InventoryQuantityRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateInventoryRequest;
import dev.ulisses.highperformanceapi.domain.entity.Inventory;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.repository.InventoryRepository;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryControllerIT extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Value("${APP_SECURITY_USERNAME}")
    private String username;

    @Value("${APP_SECURITY_PASSWORD}")
    private String password;


    @Test
    @DisplayName("Should return inventory by product id")
    void shouldReturnInventoryByProductId() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(20);

        inventory = inventoryRepository.save(inventory);

        mockMvc.perform(
                        get("/api/v1/inventories/products/{productId}", product.getId())
                                .with(httpBasic(username, password))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inventory.getId().toString()))
                .andExpect(jsonPath("$.productId").value(product.getId().toString()))
                .andExpect(jsonPath("$.availableQuantity").value(100))
                .andExpect(jsonPath("$.reservedQuantity").value(20));
    }

    @Test
    @DisplayName("Should return 404 when inventory does not exist")
    void shouldReturn404WhenInventoryNotFound() throws Exception {

        mockMvc.perform(
                        get("/api/v1/inventories/products/{productId}", UUID.randomUUID())
                                .with(httpBasic(username, password))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update inventory successfully")
    void shouldUpdateInventorySuccessfully() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(20);

        inventory = inventoryRepository.save(inventory);

        UpdateInventoryRequest request = new UpdateInventoryRequest(
                150,
                10
        );

        mockMvc.perform(
                        put("/api/v1/inventories/products/{productId}", product.getId())
                                .with(httpBasic(username, password))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inventory.getId().toString()))
                .andExpect(jsonPath("$.productId").value(product.getId().toString()))
                .andExpect(jsonPath("$.availableQuantity").value(150))
                .andExpect(jsonPath("$.reservedQuantity").value(10));

        Inventory updatedInventory = inventoryRepository
                .findByProductId(product.getId())
                .orElseThrow();

        assertEquals(150, updatedInventory.getAvailableQuantity());
        assertEquals(10, updatedInventory.getReservedQuantity());

        // Product association should never change
        assertEquals(product.getId(), updatedInventory.getProduct().getId());
    }

    @Test
    @DisplayName("Should reserve inventory successfully")
    void shouldReserveInventorySuccessfully() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(20);

        inventory = inventoryRepository.save(inventory);

        InventoryQuantityRequest request = new InventoryQuantityRequest(30);

        mockMvc.perform(
                        post("/api/v1/inventories/products/{productId}/reserve", product.getId())
                                .with(httpBasic(username, password))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        Inventory updatedInventory = inventoryRepository
                .findByProductId(product.getId())
                .orElseThrow();

        assertEquals(70, updatedInventory.getAvailableQuantity());
        assertEquals(50, updatedInventory.getReservedQuantity());

        assertEquals(product.getId(), updatedInventory.getProduct().getId());
    }

    @Test
    @DisplayName("Should return 400 when request is invalid")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(20);

        inventoryRepository.save(inventory);

        UpdateInventoryRequest request = new UpdateInventoryRequest(
                -10,
                -5
        );

        mockMvc.perform(
                        put("/api/v1/inventories/products/{productId}", product.getId())
                                .with(httpBasic(username, password))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 422 when stock is insufficient")
    void shouldReturn422WhenStockIsInsufficient() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);

        inventoryRepository.save(inventory);

        InventoryQuantityRequest request = new InventoryQuantityRequest(20);

        mockMvc.perform(
                        post("/api/v1/inventories/products/{productId}/reserve", product.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is(422));
    }

}
