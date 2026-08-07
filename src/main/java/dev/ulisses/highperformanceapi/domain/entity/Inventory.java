package dev.ulisses.highperformanceapi.domain.entity;

import dev.ulisses.highperformanceapi.application.exception.InsufficientStockException;
import jakarta.persistence.*;

@Table(
        name = "inventories",
        indexes = {
            @Index(name = "idx_inventory_product", columnList = "product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_product", columnNames = "product_id")
        }
)
@Entity
public class Inventory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_product")
    )
    private Product product;

    @Column(name = "available_quantity")
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    @Transient
    public int getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public void reserve(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (availableQuantity < quantity) {
            throw new InsufficientStockException("Insufficient inventory.");
        }

        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void release(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (reservedQuantity < quantity) {
            throw new IllegalArgumentException("Cannot release more than the reserved quantity.");
        }

        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    public void deduct(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (reservedQuantity < quantity) {
            throw new IllegalArgumentException("Cannot deduct more than the reserved quantity.");
        }

        reservedQuantity -= quantity;
    }
}

