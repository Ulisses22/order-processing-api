package dev.ulisses.highperformanceapi.application.service.cache;

import dev.ulisses.highperformanceapi.application.dto.request.UpdateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ProductResponse;
import dev.ulisses.highperformanceapi.application.service.ProductService;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Execution(ExecutionMode.SAME_THREAD)
class ProductCacheIT extends IntegrationTest {

    @Autowired
    private ProductService productService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    private Cache productsCache;

    @BeforeEach
    void setUp() {
        productsCache = cacheManager.getCache("products");

        assertNotNull(productsCache);

        productsCache.invalidate();
    }

    @AfterEach
    void tearDown() {
        productsCache.invalidate();
    }

    @Test
    void shouldCacheProductAfterFirstRead() {
        // Arrange
        Product product = createProduct();
        product = productRepository.saveAndFlush(product);

        UUID productId = product.getId();

        assertNull(productsCache.get(productId));

        // Act
        ProductResponse firstResult =
                productService.getById(productId);

        // Assert
        assertNotNull(firstResult);

        Cache.ValueWrapper cachedValue =
                productsCache.get(productId);

        assertNotNull(
                cachedValue,
                "Product should be present in Redis after first read"
        );

        ProductResponse cachedProduct =
                (ProductResponse) cachedValue.get();

        assertEquals(firstResult.id(), cachedProduct.id());
        assertEquals(firstResult.sku(), cachedProduct.sku());
        assertEquals(firstResult.name(), cachedProduct.name());
        assertEquals(firstResult.description(), cachedProduct.description());

        assertEquals(
                0,
                firstResult.price().compareTo(cachedProduct.price())
        );

        assertEquals(firstResult.status(), cachedProduct.status());
        assertEquals(firstResult.createdAt(), cachedProduct.createdAt());
        assertEquals(firstResult.updatedAt(), cachedProduct.updatedAt());
    }

    @Test
    void shouldReturnCachedProductOnSecondRead() {
        // Arrange
        Product product = createProduct();
        product = productRepository.saveAndFlush(product);

        UUID productId = product.getId();

        // First call populates Redis.
        ProductResponse firstResult =
                productService.getById(productId);

        // Act
        ProductResponse secondResult =
                productService.getById(productId);

        // Assert
        assertNotNull(firstResult);
        assertNotNull(secondResult);

        assertEquals(firstResult.id(), secondResult.id());
        assertEquals(firstResult.sku(), secondResult.sku());
        assertEquals(firstResult.name(), secondResult.name());
        assertEquals(firstResult.description(), secondResult.description());

        assertEquals(
                0,
                firstResult.price().compareTo(secondResult.price())
        );

        assertEquals(firstResult.status(), secondResult.status());
        assertEquals(firstResult.createdAt(), secondResult.createdAt());
        assertEquals(firstResult.updatedAt(), secondResult.updatedAt());

        assertNotNull(productsCache.get(productId));
    }

    @Test
    void shouldUseRedisCacheOnSecondRead() {
        // Arrange
        Product product = createProduct();
        product = productRepository.saveAndFlush(product);

        UUID productId = product.getId();

        productsCache.invalidate();

        // Ignore repository interaction used to prepare test data.
        clearInvocations(productRepository);

        // Act
        ProductResponse firstResult =
                productService.getById(productId);

        ProductResponse secondResult =
                productService.getById(productId);

        // Assert
        assertNotNull(firstResult);
        assertNotNull(secondResult);

        assertEquals(firstResult.id(), secondResult.id());
        assertEquals(firstResult.sku(), secondResult.sku());
        assertEquals(firstResult.name(), secondResult.name());
        assertEquals(firstResult.description(), secondResult.description());

        assertEquals(
                0,
                firstResult.price().compareTo(secondResult.price())
        );

        assertEquals(firstResult.status(), secondResult.status());
        assertEquals(firstResult.createdAt(), secondResult.createdAt());
        assertEquals(firstResult.updatedAt(), secondResult.updatedAt());

        // PostgreSQL should only be queried during the first request.
        verify(productRepository, times(1))
                .findById(productId);

        // Product should be present in Redis.
        assertNotNull(productsCache.get(productId));
    }

    @Test
    void shouldEvictProductFromCacheAfterUpdate() {
        // Arrange
        Product product = createProduct();
        product = productRepository.saveAndFlush(product);

        UUID productId = product.getId();

        productService.getById(productId);

        assertNotNull(productsCache.get(productId));

        UpdateProductRequest request =
                new UpdateProductRequest(
                        "Updated Product",
                        "Updated product description",
                        new BigDecimal("249.90")
                );

        // Act
        productService.update(productId, request);

        // Assert
        assertNull(
                productsCache.get(productId),
                "Product should be evicted from Redis after update"
        );
    }

    @Test
    void shouldEvictProductFromCacheAfterDelete() {
        // Arrange
        Product product = createProduct();
        product = productRepository.saveAndFlush(product);

        UUID productId = product.getId();

        productService.getById(productId);

        assertNotNull(productsCache.get(productId));

        // Act
        productService.delete(productId);

        // Assert
        assertNull(
                productsCache.get(productId),
                "Product should be evicted from Redis after delete"
        );

        assertFalse(productRepository.existsById(productId));
    }

    private Product createProduct() {
        Product product = new Product();

        product.setSku("CACHE-" + UUID.randomUUID());
        product.setName("Cached Mechanical Keyboard");
        product.setDescription(
                "Product used for Redis cache integration tests"
        );
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        return product;
    }
}
