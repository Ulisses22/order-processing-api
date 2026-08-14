package dev.ulisses.highperformanceapi.domain.specification;

import dev.ulisses.highperformanceapi.application.dto.request.PaymentSearchRequest;
import dev.ulisses.highperformanceapi.domain.entity.Payment;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PaymentSpecification {

    private PaymentSpecification() {
    }

    public static Specification<Payment> withFilters(
            PaymentSearchRequest request
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (request.orderId() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("order").get("id"),
                                request.orderId()
                        )
                );
            }

            if (request.status() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                request.status()
                        )
                );
            }

            if (request.paymentMethod() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("method"),
                                request.paymentMethod()
                        )
                );
            }

            if (request.transactionId() != null
                    && !request.transactionId().isBlank()) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("transactionId"),
                                request.transactionId()
                        )
                );
            }

            if (request.createdFrom() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                request.createdFrom()
                        )
                );
            }

            if (request.createdTo() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("createdAt"),
                                request.createdTo()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}