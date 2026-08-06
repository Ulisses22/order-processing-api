package dev.ulisses.highperformanceapi.application.service;


import dev.ulisses.highperformanceapi.application.dto.request.CreateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ProductResponse;
import dev.ulisses.highperformanceapi.application.exception.DuplicateResourceException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.ProductMapper;
import dev.ulisses.highperformanceapi.application.service.impl.ProductServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository  productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;


    @Test
    void shouldCreateProduct() {
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "Mechanical Keyboard",
                "SKU-001",
                "Mechanical gaming keyboard with RGB switches",
                new BigDecimal("199.90")
        );

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSku(request.sku());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setStatus(ProductStatus.ACTIVE);

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                new BigDecimal("199.90"),
                ProductStatus.ACTIVE,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );

        when(productRepository.existsBySku(request.sku())).thenReturn(false);
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        // Act
        ProductResponse result = productService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);

        verify(productRepository).existsBySku(request.sku());
        verify(productMapper).toEntity(request);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenSkuAlreadyExists() {
        // Arrange
        CreateProductRequest request = new CreateProductRequest(
                "SKU-001",
                "Mechanical Keyboard",
                "Mechanical gaming keyboard with RGB switches",
                new BigDecimal("199.90")
        );

        when(productRepository.existsBySku(request.sku()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                DuplicateResourceException.class,
                () -> productService.create(request)
        );

        verify(productRepository).existsBySku(request.sku());

        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toEntity(any(CreateProductRequest.class));
        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void shouldReturnProductById() {
        // Arrange
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard with RGB switches");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productMapper.toResponse(product))
                .thenReturn(response);

        // Act
        ProductResponse result = productService.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);

        verify(productRepository).findById(productId);
        verify(productMapper).toResponse(product);

        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenProductDoesNotExist() {
        // Arrange
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getById(productId)
        );

        assertEquals(
                "Product with id '%s' not found".formatted(productId),
                exception.getMessage()
        );

        verify(productRepository).findById(productId);

        verify(productMapper, never()).toResponse(any(Product.class));

        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    void shouldReturnPagedProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSku("SKU-001");
        product.setName("Mechanical Keyboard");
        product.setDescription("Mechanical gaming keyboard with RGB switches");
        product.setPrice(new BigDecimal("199.90"));
        product.setStatus(ProductStatus.ACTIVE);

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );

        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productRepository.findAll(pageable))
                .thenReturn(productPage);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        // Act
        Page<ProductResponse> result = productService.getAll(pageable);

        // Assert
        assertNotNull(result);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        assertEquals(response, result.getContent().getFirst());

        verify(productRepository).findAll(pageable);
        verify(productMapper).toResponse(product);

        verifyNoMoreInteractions(productRepository, productMapper);
    }
}
