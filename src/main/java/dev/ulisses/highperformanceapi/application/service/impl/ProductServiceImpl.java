package dev.ulisses.highperformanceapi.application.service.impl;


import dev.ulisses.highperformanceapi.application.dto.request.CreateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ProductResponse;
import dev.ulisses.highperformanceapi.application.exception.DuplicateResourceException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.ProductMapper;
import dev.ulisses.highperformanceapi.application.service.ProductService;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import dev.ulisses.highperformanceapi.domain.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ProductResponse create(CreateProductRequest request) {

        if(productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product with sku %s already exists".formatted(request.sku()));
        }

        Product product = productMapper.toEntity(request);

        product = productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Product with id '%s' not found".formatted(id)
                )
        );

        productMapper.updateEntity(request, product);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    public ProductResponse getById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product with id '%s' not found".formatted(id))
        );
        return productMapper.toResponse(product);
    }

    @Override
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String name, Pageable pageable) {
        Specification<Product> specification = Specification
                .where(ProductSpecification.withFilters(name));
        return productRepository
                .findAll(specification, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    public void delete(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product with id '%s' not found".formatted(id))
        );
        productRepository.delete(product);
    }
}
