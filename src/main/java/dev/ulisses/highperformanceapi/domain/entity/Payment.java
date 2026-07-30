package dev.ulisses.highperformanceapi.domain.entity;

import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_order", columnList = "order_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_transaction", columnList = "transaction_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_transaction",
                        columnNames = "transaction_id"
                )
        }
)
@Entity
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_order")
    )
    private Order order;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;

    @Column(name = "authorization_code", length = 100)
    private String authorizationCode;

    @Column(name = "processed_at")
    private Instant processedAt;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAuthorizationCode(){
        return  authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
