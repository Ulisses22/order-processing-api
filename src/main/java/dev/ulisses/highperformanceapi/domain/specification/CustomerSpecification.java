package dev.ulisses.highperformanceapi.domain.specification;

import dev.ulisses.highperformanceapi.domain.entity.Customer;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecification {

    private CustomerSpecification() {
    }

    public static Specification<Customer> hasName(String name) {
        return (root, query, criteriaBuilder) -> {

            if (name == null || name.isBlank()) {
                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Customer> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {

            if (email == null || email.isBlank()) {
                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%"
            );
        };
    }

}
