package dev.ulisses.highperformanceapi.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisConnectionFactoryIT {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCreateRedisConnectionFactory() {
        assertNotNull(redisConnectionFactory);
    }

    @Test
    void shouldConnectToRedis() {
        assertNotNull(redisConnectionFactory);

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            assertEquals("PONG", connection.ping());
        }
    }

    @Test
    void shouldCreateRedisCacheManager() {
        assertNotNull(cacheManager);
        assertInstanceOf(RedisCacheManager.class, cacheManager);
    }
}
