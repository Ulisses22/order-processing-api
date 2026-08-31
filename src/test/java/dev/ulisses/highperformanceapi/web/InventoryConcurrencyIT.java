package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.InventoryQuantityRequest;
import dev.ulisses.highperformanceapi.domain.entity.Inventory;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.repository.InventoryRepository;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryConcurrencyIT extends IntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @AfterEach
    void cleanup() {
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should prevent concurrent inventory reservation")
    void shouldPreventConcurrentInventoryReservation() throws Exception {

        Product product = new Product();
        product.setSku("SKU-CONCURRENT");
        product.setName("Concurrent Product");
        product.setDescription("Optimistic locking test");
        product.setPrice(new BigDecimal("10.00"));
        product.setStatus(ProductStatus.ACTIVE);

        product = productRepository.save(product);

        final UUID productId = product.getId();

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(0);

        inventoryRepository.saveAndFlush(inventory);

        int threads = 20;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {

            futures.add(executor.submit(() -> {

                ready.countDown();

                start.await();

                InventoryQuantityRequest request =
                        new InventoryQuantityRequest(10);

                return mockMvc.perform(
                                post("/api/v1/inventories/products/{productId}/reserve",
                                        productId)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticate())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andReturn()
                        .getResponse()
                        .getStatus();
            }));
        }

        ready.await();

        start.countDown();

        int success = 0;
        int insufficientStock = 0;
        int optimisticLock = 0;

        for (Future<Integer> future : futures) {

            int status = future.get(10, TimeUnit.SECONDS);

            switch (status) {
                case 200 -> success++;
                case 409 -> insufficientStock++;
                case 500 -> optimisticLock++; // Should become 0 after retries
                default -> fail("Unexpected HTTP status: " + status);
            }
        }

        System.out.printf("SUCCESS=%d, INSUFFICIENT_STOCK=%d, OPTIMISTIC_LOCK=%d%n", success, insufficientStock, optimisticLock);

        executor.shutdown();

        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        Inventory updated = inventoryRepository
                .findByProductId(productId)
                .orElseThrow();

        assertAll(
                () -> assertTrue(updated.getAvailableQuantity() >= 0),
                () -> assertTrue(updated.getReservedQuantity() >= 0),
                () -> assertTrue(updated.getReservedQuantity() <= 100),
                () -> assertEquals(
                        100,
                        updated.getAvailableQuantity() + updated.getReservedQuantity()
                )
        );
        assertEquals(
                100,
                updated.getAvailableQuantity() + updated.getReservedQuantity()
        );
    }
}
