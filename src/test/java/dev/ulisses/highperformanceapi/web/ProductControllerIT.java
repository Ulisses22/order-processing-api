package dev.ulisses.highperformanceapi.web;


import dev.ulisses.highperformanceapi.application.dto.request.*;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerIT extends IntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() throws Exception {

        CreateProductRequest request = new CreateProductRequest(
                "SKU-001",
                "Mechanical Keyboard",
                "Mechanical gaming keyboard with RGB switches",
                new BigDecimal("199.90")
        );

        mockMvc.perform(
                        post("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.description")
                        .value("Mechanical gaming keyboard with RGB switches"))
                .andExpect(jsonPath("$.price").value(199.90))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Product product = productRepository
                .findBySku("SKU-001")
                .orElseThrow();

        assertEquals("Mechanical Keyboard", product.getName());
        assertEquals(
                "Mechanical gaming keyboard with RGB switches",
                product.getDescription()
        );
        assertEquals(new BigDecimal("199.90"), product.getPrice());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    @Test
    @DisplayName("Should search products by name")
    void shouldSearchProductsByName() throws Exception {

        Product keyboard = new Product();
        keyboard.setSku("SKU-001");
        keyboard.setName("Mechanical Keyboard");
        keyboard.setDescription("Gaming keyboard");
        keyboard.setPrice(new BigDecimal("199.90"));
        keyboard.setStatus(ProductStatus.ACTIVE);

        Product mouse = new Product();
        mouse.setSku("SKU-002");
        mouse.setName("Gaming Mouse");
        mouse.setDescription("Wireless mouse");
        mouse.setPrice(new BigDecimal("59.90"));
        mouse.setStatus(ProductStatus.ACTIVE);

        productRepository.save(keyboard);
        productRepository.save(mouse);

        mockMvc.perform(
                        get("/api/v1/products")
                                .param("name", "Keyboard")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Mechanical Keyboard"));
    }

    @Test
    @DisplayName("Should find product by id")
    void shouldFindProductById() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard with RGB switches");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        mockMvc.perform(
                        get("/api/v1/products/{id}", product.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId().toString()))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.description")
                        .value("Mechanical gaming keyboard with RGB switches"))
                .andExpect(jsonPath("$.price").value(199.90))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should return 404 when product does not exist")
    void shouldReturn404WhenProductNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/products/{id}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate()))
                .andExpect(status().isNotFound()
        );
    }

    @Test
    @DisplayName("Should return paged products")
    void shouldReturnPagedProducts() throws Exception {

        Product p1 = new Product();
        p1.setSku("SKU-001");
        p1.setName("Mechanical Keyboard");
        p1.setDescription("Mechanical gaming keyboard");
        p1.setPrice(new BigDecimal("199.90"));
        p1.setStatus(ProductStatus.ACTIVE);

        Product p2 = new Product();
        p2.setSku("SKU-002");
        p2.setName("Gaming Mouse");
        p2.setDescription("Wireless gaming mouse");
        p2.setPrice(new BigDecimal("59.90"));
        p2.setStatus(ProductStatus.ACTIVE);

        productRepository.save(p1);
        productRepository.save(p2);

        mockMvc.perform(
                        get("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        UpdateProductRequest request = new UpdateProductRequest(
                "Gaming Keyboard",
                "Updated RGB mechanical keyboard",
                new BigDecimal("249.90")
        );

        mockMvc.perform(
                        put("/api/v1/products/{id}", product.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId().toString()))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Gaming Keyboard"))
                .andExpect(jsonPath("$.description")
                        .value("Updated RGB mechanical keyboard"))
                .andExpect(jsonPath("$.price").value(249.90))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertEquals("Gaming Keyboard", updatedProduct.getName());
        assertEquals(
                "Updated RGB mechanical keyboard",
                updatedProduct.getDescription()
        );
        assertEquals(
                new BigDecimal("249.90"),
                updatedProduct.getPrice()
        );

        // SKU should never change
        assertEquals("SKU-001", updatedProduct.getSku());

        // Status should remain ACTIVE
        assertEquals(ProductStatus.ACTIVE, updatedProduct.getStatus());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProductSuccessfully() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        mockMvc.perform(
                        delete("/api/v1/products/{id}", product.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                )
                .andExpect(status().isNoContent());

        assertFalse(productRepository.existsById(product.getId()));
    }

    @Test
    @DisplayName("Should return 400 when request is invalid")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {

        CreateProductRequest request = new CreateProductRequest(
                "",
                "",
                "",
                BigDecimal.ZERO
        );

        mockMvc.perform(
                        post("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when SKU already exists")
    void shouldReturn409WhenSkuAlreadyExists() throws Exception {

        Product product = new Product();
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        productRepository.save(product);

        CreateProductRequest request = new CreateProductRequest(
                "SKU-001",
                "Gaming Keyboard",
                "Another keyboard with the same SKU",
                new BigDecimal("249.90")
        );

        mockMvc.perform(
                        post("/api/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 403 when user does not have required authority")
    @WithMockUser(username = "user", authorities = "USER")
    void shouldReturn403WhenUserDoesNotHaveRequiredAuthority() throws Exception {

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Test Product",
                              "sku": "TEST-001",
                              "price": 19.99
                            }
                            """))
                .andExpect(status().isForbidden());
    }
}
