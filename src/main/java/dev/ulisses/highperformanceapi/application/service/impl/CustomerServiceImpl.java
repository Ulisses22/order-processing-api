package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.dto.request.CreateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.response.CustomerResponse;
import dev.ulisses.highperformanceapi.application.exception.DuplicateResourceException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.CustomerMapper;
import dev.ulisses.highperformanceapi.application.service.CustomerService;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import dev.ulisses.highperformanceapi.domain.specification.CustomerSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    public CustomerResponse create(CreateCustomerRequest request) {

        if(customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Customer with email '%s' already exists.".formatted(request.email()));
        }
        Customer customer = customerMapper.toEntity(request);

        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id '%s' not found.".formatted(id)
                ));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(customerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(String name, String email, Pageable pageable) {
        Specification<Customer> specification = Specification
                .where(CustomerSpecification.hasName(name))
                .and(CustomerSpecification.hasEmail(email));
        return customerRepository.findAll(specification, pageable)
                .map(customerMapper::toResponse);
    }

    @Override
    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id '%s' not found.".formatted(id)
                ));
        customerMapper.updateEntity(request, customer);

        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    public void delete(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id '%s' not found.".formatted(id)
                ));
        customerRepository.delete(customer);

        // TODO: future-proof version
//        if (customer.hasOrders()) {
//            throw new BusinessException(
//                    "Customer cannot be deleted because it has associated orders."
//            );
//        }
        // Or use soft delete without change the repository
//        customer.setStatus(CustomerStatus.DELETED);
//
//        customerRepository.save(customer);

    }
}
