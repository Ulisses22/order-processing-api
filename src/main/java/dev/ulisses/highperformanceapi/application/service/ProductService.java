package dev.ulisses.highperformanceapi.application.service;


import dev.ulisses.highperformanceapi.application.dto.request.CreateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    ProductResponse update(UUID id, UpdateProductRequest request);

    ProductResponse getById(UUID id);

    Page<ProductResponse> getAll(Pageable pageable);

    Page<ProductResponse> search(String name, Pageable pageable);

    void delete(UUID id);
}
