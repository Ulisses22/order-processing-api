package dev.ulisses.highperformanceapi.application.service.cache;

import dev.ulisses.highperformanceapi.application.dto.request.UpdateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.response.CustomerResponse;
import dev.ulisses.highperformanceapi.application.service.CustomerService;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Execution(ExecutionMode.SAME_THREAD)
class CustomerCacheIT extends IntegrationTest {

    @Autowired
    private CustomerService customerService;

    @MockitoSpyBean
    private CustomerRepository customerRepository;

    @Autowired
    private CacheOperationSource cacheOperationSource;

    @Autowired
    private CacheManager cacheManager;

    private Cache customersCache;

    @BeforeEach
    void setUp() {
        customersCache = cacheManager.getCache("customers");

        assertNotNull(customersCache);

        customersCache.invalidate();
    }

    @AfterEach
    void tearDown() {
        customersCache.invalidate();
    }

    @Test
    void shouldCacheCustomerAfterFirstRead() {
        // Arrange
        Customer customer = createCustomer();
        customer = customerRepository.saveAndFlush(customer);

        UUID customerId = customer.getId();

        assertNull(customersCache.get(customerId));

        // Act
        CustomerResponse firstResult =
                customerService.findById(customerId);

        // Assert
        assertNotNull(firstResult);

        Cache.ValueWrapper cachedValue =
                customersCache.get(customerId);

        assertNotNull(cachedValue);

        CustomerResponse cachedCustomer =
                (CustomerResponse) cachedValue.get();

        assertEquals(firstResult.id(), cachedCustomer.id());
        assertEquals(firstResult.firstName(), cachedCustomer.firstName());
        assertEquals(firstResult.lastName(), cachedCustomer.lastName());
        assertEquals(firstResult.email(), cachedCustomer.email());
        assertEquals(firstResult.status(), cachedCustomer.status());
        assertEquals(firstResult.createdAt(), cachedCustomer.createdAt());
        assertEquals(firstResult.updatedAt(), cachedCustomer.updatedAt());
    }

    @Test
    void shouldReturnCachedCustomerOnSecondRead() {
        // Arrange
        Customer customer = createCustomer();
        customer = customerRepository.saveAndFlush(customer);

        UUID customerId = customer.getId();

        // First call populates Redis
        CustomerResponse firstResult =
                customerService.findById(customerId);

        // Act
        CustomerResponse secondResult =
                customerService.findById(customerId);

        // Assert
        assertNotNull(firstResult);
        assertNotNull(secondResult);

        assertEquals(firstResult.id(), secondResult.id());
        assertEquals(firstResult.firstName(), secondResult.firstName());
        assertEquals(firstResult.lastName(), secondResult.lastName());
        assertEquals(firstResult.email(), secondResult.email());
        assertEquals(firstResult.status(), secondResult.status());
        assertEquals(firstResult.createdAt(), secondResult.createdAt());
        assertEquals(firstResult.updatedAt(), secondResult.updatedAt());

        assertNotNull(customersCache.get(customerId));
    }

    @Test
    void shouldUseRedisCacheOnSecondRead() {
        // Arrange
        Customer customer = createCustomer();
        customer = customerRepository.saveAndFlush(customer);

        UUID customerId = customer.getId();

        customersCache.invalidate();

        // Ignore repository interaction used to prepare test data.
        clearInvocations(customerRepository);

        // Act
        CustomerResponse firstResult =
                customerService.findById(customerId);

        CustomerResponse secondResult =
                customerService.findById(customerId);

        // Assert
        assertNotNull(firstResult);
        assertNotNull(secondResult);

        assertEquals(firstResult.id(), secondResult.id());
        assertEquals(firstResult.firstName(), secondResult.firstName());
        assertEquals(firstResult.lastName(), secondResult.lastName());
        assertEquals(firstResult.email(), secondResult.email());
        assertEquals(firstResult.status(), secondResult.status());
        assertEquals(firstResult.createdAt(), secondResult.createdAt());
        assertEquals(firstResult.updatedAt(), secondResult.updatedAt());

        // PostgreSQL should only be queried during the first request.
        verify(customerRepository, times(1))
                .findById(customerId);

        // Customer should be present in Redis.
        assertNotNull(customersCache.get(customerId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldEvictCustomerFromCacheAfterUpdate() {
        // Arrange
        Customer customer = createCustomer();
        customer = customerRepository.saveAndFlush(customer);

        UUID customerId = customer.getId();

        customerService.findById(customerId);

        assertNotNull(customersCache.get(customerId));

        UpdateCustomerRequest request =
                new UpdateCustomerRequest(
                        "Updated",
                        "Customer"
                );

        // Act
        customerService.update(customerId, request);

        // Assert
        assertNull(
                customersCache.get(customerId),
                "Customer should be evicted from Redis after update"
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldEvictCustomerFromCacheAfterDelete() {
        // Arrange
        Customer customer = createCustomer();
        customer = customerRepository.saveAndFlush(customer);

        UUID customerId = customer.getId();

        customerService.findById(customerId);

        assertNotNull(customersCache.get(customerId));

        // Act
        customerService.delete(customerId);

        // Assert
        assertNull(
                customersCache.get(customerId),
                "Customer should be evicted from Redis after delete"
        );

        assertFalse(customerRepository.existsById(customerId));
    }

    private Customer createCustomer() {
        Customer customer = new Customer();

        customer.setFirstName("Cache");
        customer.setLastName("Customer");
        customer.setEmail(
                "cache-" + UUID.randomUUID() + "@example.com"
        );
        customer.setStatus(CustomerStatus.ACTIVE);

        return customer;
    }
}
