package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    CustomerResponse create(CreateCustomerRequest request);

    CustomerResponse findById(UUID id);

    Page<CustomerResponse> findAll(Pageable pageable);

    Page<CustomerResponse> search(
            String name,
            String email,
            Pageable pageable
    );

    CustomerResponse update(UUID id, UpdateCustomerRequest request);

    void delete(UUID id);

}
