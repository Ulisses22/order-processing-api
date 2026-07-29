package dev.ulisses.highperformanceapi.domain.entity;

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

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    @Transient
    public int getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public void reserve(Integer quantity) {
        if (availableQuantity < quantity) {
            throw new IllegalArgumentException("Insufficient inventory.");
        }

        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void release(Integer quantity) {
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    public void deduct(Integer quantity) {
        reservedQuantity -= quantity;
    }
}

