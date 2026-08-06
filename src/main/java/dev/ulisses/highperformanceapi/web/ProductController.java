package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.CreateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateProductRequest;
import dev.ulisses.highperformanceapi.application.dto.response.ProductResponse;
import dev.ulisses.highperformanceapi.application.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.create(request);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(
            @PathVariable String id
    ) {
        return productService.getById(java.util.UUID.fromString(id));
    }

    @GetMapping
    public Page<ProductResponse> getAll(
            @RequestParam(required = false) String name,
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt"
            ) Pageable pageable
    ) {
        if (name == null || name.isBlank()) {
            return productService.getAll(pageable);
        }

        return productService.search(name, pageable);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.update(
                java.util.UUID.fromString(id),
                request
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String id
    ) {
        productService.delete(java.util.UUID.fromString(id));
    }
}
