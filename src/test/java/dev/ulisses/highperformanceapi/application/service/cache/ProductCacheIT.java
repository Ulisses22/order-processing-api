package dev.ulisses.highperformanceapi.application.service.cache;

import dev.ulisses.highperformanceapi.application.dto.response.ProductResponse;
import dev.ulisses.highperformanceapi.application.service.impl.ProductServiceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductCacheIT extends IntegrationTest {

    @Autowired
    private ProductServiceImpl productService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    private Cache productsCache;

    @BeforeEach
    void setUp() {
        productsCache = cacheManager.getCache("products");

        assertNotNull(productsCache);

        productsCache.clear();
    }

    @Test
    void shouldCacheProductAfterFirstRead() {
        // Arrange
        Product product = createProduct();

        product = productRepository.saveAndFlush(product);

        UUID productId = product.getId();

        assertNull(productsCache.get(productId));

        // Act
        ProductResponse firstResult = productService.getById(productId);

        // Assert
        assertNotNull(firstResult);

        Cache.ValueWrapper cachedValue = productsCache.get(productId);

        assertNotNull(cachedValue);

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

        // First call populates Redis
        ProductResponse firstResult = productService.getById(productId);

        // Act
        ProductResponse secondResult = productService.getById(productId);

        // Assert
        assertNotNull(firstResult);
        assertNotNull(secondResult);
        assertEquals(firstResult, secondResult);

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

        // Act
        // We will complete this test with the actual UpdateProductRequest
        // after verifying the cache population behaviour.
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
        assertNull(productsCache.get(productId));
        assertFalse(productRepository.existsById(productId));
    }

    private Product createProduct() {
        Product product = new Product();

        product.setSku("CACHE-" + UUID.randomUUID());
        product.setName("Cached Mechanical Keyboard");
        product.setDescription("Product used for Redis cache integration tests");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        return product;
    }

    @Test
    void shouldUseRedisCacheOnSecondRead() {
        // Arrange
        Product product = createProduct();

        product = productRepository.saveAndFlush(product);

        UUID productId = product.getId();

        productsCache.clear();

        // Clear repository interactions from test data preparation.
        clearInvocations(productRepository);

        // Act
        ProductResponse firstResult = productService.getById(productId);
        ProductResponse secondResult = productService.getById(productId);

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

        // Redis should contain the cached response. (means: Product should be present)
        assertNotNull(productsCache.get(productId));
    }
}
