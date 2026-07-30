package dev.ulisses.highperformanceapi.domain.entity;

import dev.ulisses.highperformanceapi.domain.enums.ShipmentStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "shipments",
        indexes = {
                @Index(name = "idx_shipment_order", columnList = "order_id"),
                @Index(name = "idx_shipment_status", columnList = "status"),
                @Index(name = "idx_shipment_tracking", columnList = "tracking_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shipment_tracking",
                        columnNames = "tracking_number"
                )
        }
)
public class Shipment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_shipment_order")
    )
    private Order order;

    @Column(nullable = false, length = 100)
    private String carrier;

    @Column(name = "tracking_number", nullable = false, length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress() {
        this.shippingAddress = shippingAddress;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(Instant shippedAt) {
        this.shippedAt = shippedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}
