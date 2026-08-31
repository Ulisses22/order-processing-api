package dev.ulisses.highperformanceapi.domain.repository;

import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import dev.ulisses.highperformanceapi.domain.specification.ProductSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(ProductRepositoryIT.TestCacheConfiguration.class)
class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;

    @TestConfiguration
    static class TestCacheConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Test
    void shouldFindProductBySku() {
        Product product = createProduct(
                "SKU-001",
                "Laptop",
                ProductStatus.ACTIVE
        );

        productRepository.saveAndFlush(product);

        Optional<Product> result =
                productRepository.findBySku("SKU-001");

        assertThat(result)
                .isPresent()
                .get()
                .extracting(Product::getSku)
                .isEqualTo("SKU-001");
    }

    @Test
    void shouldReturnTrueWhenSkuExists() {
        Product product = createProduct(
                "SKU-002",
                "Keyboard",
                ProductStatus.ACTIVE
        );

        productRepository.saveAndFlush(product);

        assertThat(
                productRepository.existsBySku("SKU-002")
        ).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSkuDoesNotExist() {
        assertThat(
                productRepository.existsBySku("UNKNOWN-SKU")
        ).isFalse();
    }

    @Test
    void shouldFindProductsUsingNameSpecification() {
        Product laptop = createProduct(
                "SKU-003",
                "Gaming Laptop",
                ProductStatus.ACTIVE
        );

        Product keyboard = createProduct(
                "SKU-004",
                "Mechanical Keyboard",
                ProductStatus.ACTIVE
        );

        Product phone = createProduct(
                "SKU-005",
                "Smartphone",
                ProductStatus.ACTIVE
        );

        productRepository.save(laptop);
        productRepository.save(keyboard);
        productRepository.saveAndFlush(phone);

        var result = productRepository.findAll(
                ProductSpecification.withFilters("LAPTOP")
        );

        assertThat(result)
                .hasSize(1)
                .extracting(Product::getName)
                .containsExactly("Gaming Laptop");
    }

    private Product createProduct(
            String sku,
            String name,
            ProductStatus status
    ) {
        Product product = new Product();

        product.setSku(sku);
        product.setName(name);
        product.setDescription("Test product");
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setStatus(status);

        return product;
    }
}
