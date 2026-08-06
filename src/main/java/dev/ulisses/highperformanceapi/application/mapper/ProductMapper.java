package dev.ulisses.highperformanceapi.application.mapper;

import dev.ulisses.highperformanceapi.application.dto.request.CreateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ProductResponse;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Product toEntity(CreateProductRequest request);

    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "description", ignore = true)
    void updateEntity(
            UpdateProductRequest request,
            @MappingTarget Product product
    );
}
