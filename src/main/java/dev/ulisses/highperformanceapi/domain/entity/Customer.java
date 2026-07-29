package dev.ulisses.highperformanceapi.domain.entity;

import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import jakarta.persistence.*;


@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customer_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customer_email", columnNames = "email")
        }
)
@Entity
public class Customer extends BaseEntity{

    @Column(nullable = false, length = 100)
    private  String firstName;

    @Column(nullable = false, length = 100)
    private  String lastName;

    @Column(nullable = false, length = 255)
    private  String email;

    @Column(length = 100)
    private  String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email){
        this.email = email.toLowerCase().trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }
}
