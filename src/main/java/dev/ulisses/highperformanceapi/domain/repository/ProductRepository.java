package dev.ulisses.highperformanceapi.domain.repository;

import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

}
