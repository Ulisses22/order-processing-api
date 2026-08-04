package dev.ulisses.highperformanceapi.domain.repository;

import dev.ulisses.highperformanceapi.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Optional<Customer> findByEmail(String email);

}
