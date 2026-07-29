package dev.ulisses.highperformanceapi.domain.entity;


import dev.ulisses.highperformanceapi.domain.enums.NotificationStatus;
import dev.ulisses.highperformanceapi.domain.enums.NotificationType;
import jakarta.persistence.*;

@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_customer", columnList = "customer_id"),
                @Index(name = "idx_notification_status", columnList = "status"),
                @Index(name = "idx_notification_type", columnList = "type")
        }
)
@Entity
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_customer")
    )
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, length = 5000)
    private String message;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject.trim();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
