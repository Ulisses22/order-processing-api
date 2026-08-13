package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.UpdateInventoryRequest;
import dev.ulisses.highperformanceapi.application.dto.response.InventoryResponse;
import dev.ulisses.highperformanceapi.application.exception.InsufficientStockException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.InventoryMapper;
import dev.ulisses.highperformanceapi.application.service.impl.InventoryServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.Inventory;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private UUID productId;
    private Inventory inventory;
    private InventoryResponse response;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);

        inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(0);

        response = new InventoryResponse(
                UUID.randomUUID(),
                productId,
                100,
                0,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void shouldReturnInventoryWhenProductExists() {

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        when(inventoryMapper.toResponse(inventory))
                .thenReturn(response);

        InventoryResponse result = inventoryService.getByProductId(productId);

        assertNotNull(result);
        assertEquals(response, result);

        verify(inventoryRepository).findByProductId(productId);
        verify(inventoryMapper).toResponse(inventory);
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.getByProductId(productId)
        );

        verify(inventoryRepository).findByProductId(productId);
        verifyNoInteractions(inventoryMapper);
    }

    @Test
    void shouldUpdateInventoryStock() {

        UpdateInventoryRequest request =
                new UpdateInventoryRequest(150, 20);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.saveAndFlush(any(Inventory.class)))
                .thenReturn(inventory);

        when(inventoryMapper.toResponse(inventory))
                .thenReturn(response);

        inventoryService.updateStock(productId, request);

        assertEquals(150, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());

        verify(inventoryRepository).saveAndFlush(inventory);
        verify(inventoryMapper).toResponse(inventory);
    }

    @Test
    void shouldReserveStock() {

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        inventoryService.reserveStock(productId, 20);

        assertEquals(80, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());

        verify(inventoryRepository).saveAndFlush(inventory);
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        inventory.setAvailableQuantity(5);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reserveStock(productId, 10)
        );

        verify(inventoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldReleaseStock() {

        inventory.setAvailableQuantity(80);
        inventory.setReservedQuantity(20);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        inventoryService.releaseStock(productId, 10);

        assertEquals(90, inventory.getAvailableQuantity());
        assertEquals(10, inventory.getReservedQuantity());

        verify(inventoryRepository).saveAndFlush(inventory);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingInventoryThatDoesNotExist() {

        UpdateInventoryRequest request =
                new UpdateInventoryRequest(100, 0);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.updateStock(productId, request)
        );

        verify(inventoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowExceptionWhenReleasingInventoryThatDoesNotExist() {

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.releaseStock(productId, 10)
        );

        verify(inventoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowExceptionWhenReservingInventoryThatDoesNotExist() {

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.reserveStock(productId, 10)
        );

        verify(inventoryRepository, never()).saveAndFlush(any());
    }

}

