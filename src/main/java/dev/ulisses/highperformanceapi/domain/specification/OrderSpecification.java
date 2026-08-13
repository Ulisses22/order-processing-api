package dev.ulisses.highperformanceapi.domain.specification;

import dev.ulisses.highperformanceapi.application.dto.request.OrderSearchRequest;
import dev.ulisses.highperformanceapi.domain.entity.Order;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> withFilters(OrderSearchRequest request) {

        return Specification
                .where(hasCustomerId(request.customerId()))
                .and(hasStatus(request.status()))
                .and(hasOrderNumber(request.orderNumber()))
                .and(createdAfter(request.createdFrom()))
                .and(createdBefore(request.createdTo()));
    }

    private static Specification<Order> hasCustomerId(java.util.UUID customerId) {

        return (root, query, cb) ->
                customerId == null
                        ? null
                        : cb.equal(root.get("customer").get("id"), customerId);
    }

    private static Specification<Order> hasStatus(dev.ulisses.highperformanceapi.domain.enums.OrderStatus status) {

        return (root, query, cb) ->
                status == null
                        ? null
                        : cb.equal(root.get("status"), status);
    }

    private static Specification<Order> hasOrderNumber(String orderNumber) {

        return (root, query, cb) ->
                orderNumber == null || orderNumber.isBlank()
                        ? null
                        : cb.like(
                        cb.lower(root.get("orderNumber")),
                        "%" + orderNumber.toLowerCase() + "%"
                );
    }

    private static Specification<Order> createdAfter(java.time.Instant createdFrom) {

        return (root, query, cb) ->
                createdFrom == null
                        ? null
                        : cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        createdFrom
                );
    }

    private static Specification<Order> createdBefore(java.time.Instant createdTo) {

        return (root, query, cb) ->
                createdTo == null
                        ? null
                        : cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        createdTo
                );
    }
}