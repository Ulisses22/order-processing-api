package dev.ulisses.highperformanceapi.domain.entity;

import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Table(
    name = "products",
    indexes = {
            @Index(name = "idx_product_sku", columnList = "sku"),
            @Index(name = "idx_product_name", columnList = "name"),
            @Index(name = "idx_product_status", columnList = "status")
    },
    uniqueConstraints = {
            @UniqueConstraint(name = "uk_product_sku", columnNames = "sku")
    }
)
@Entity
public class Product extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

}
