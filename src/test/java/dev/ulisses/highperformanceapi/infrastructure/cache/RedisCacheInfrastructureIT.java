package dev.ulisses.highperformanceapi.infrastructure.cache;

import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;

import static org.junit.jupiter.api.Assertions.*;

class RedisCacheInfrastructureIT extends IntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCreateRedisCacheManager() {

        assertNotNull(cacheManager);

        assertInstanceOf(
                RedisCacheManager.class,
                cacheManager
        );
    }

    @Test
    void shouldCreateProductAndCustomerCaches() {

        Cache productsCache =
                cacheManager.getCache("products");

        Cache customersCache =
                cacheManager.getCache("customers");

        assertNotNull(
                productsCache,
                "Products cache should be configured"
        );

        assertNotNull(
                customersCache,
                "Customers cache should be configured"
        );
    }

    @Test
    void shouldUseRedisCacheImplementation() {

        Cache productsCache =
                cacheManager.getCache("products");

        Cache customersCache =
                cacheManager.getCache("customers");

        assertInstanceOf(
                RedisCache.class,
                productsCache
        );

        assertInstanceOf(
                RedisCache.class,
                customersCache
        );
    }

    @Test
    void shouldUseExpectedCacheNames() {

        Cache productsCache =
                cacheManager.getCache("products");

        Cache customersCache =
                cacheManager.getCache("customers");

        assertEquals(
                "products",
                productsCache.getName()
        );

        assertEquals(
                "customers",
                customersCache.getName()
        );
    }

    @Test
    void shouldAllowCacheOperations() {

        Cache productsCache =
                cacheManager.getCache("products");

        Cache customersCache =
                cacheManager.getCache("customers");

        assertNotNull(productsCache);
        assertNotNull(customersCache);

        productsCache.clear();
        customersCache.clear();

        assertNull(
                productsCache.get("infrastructure-test")
        );

        assertNull(
                customersCache.get("infrastructure-test")
        );
    }
}
