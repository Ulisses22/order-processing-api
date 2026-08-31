package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.response.CustomerResponse;
import dev.ulisses.highperformanceapi.application.exception.DuplicateResourceException;
import dev.ulisses.highperformanceapi.application.exception.ResourceNotFoundException;
import dev.ulisses.highperformanceapi.application.mapper.CustomerMapper;
import dev.ulisses.highperformanceapi.application.service.impl.CustomerServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomerSuccessfully() {

        CreateCustomerRequest request = buildCreateRequest();
        Customer customer = buildCustomer();
        CustomerResponse response = buildResponse(customer);

        when(customerRepository.existsByEmail(request.email())).thenReturn(false);
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(response);

        CustomerResponse result = customerService.create(request);

        assertNotNull(result);
        assertEquals(response.id(), result.id());

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);

        verify(customerRepository).save(captor.capture());

        Customer savedCustomer = captor.getValue();

        assertEquals(CustomerStatus.ACTIVE, savedCustomer.getStatus());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowDuplicateResourceExceptionWhenEmailAlreadyExists() {

        CreateCustomerRequest request = buildCreateRequest();

        when(customerRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.create(request));

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find customer by id")
    void shouldFindCustomerById() {

        UUID id = UUID.randomUUID();

        Customer customer = buildCustomer();
        CustomerResponse response = buildResponse(customer);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        when(customerMapper.toResponse(customer)).thenReturn(response);

        CustomerResponse result = customerService.findById(id);

        assertNotNull(result);

        verify(customerRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer does not exist")
    void shouldThrowResourceNotFoundWhenCustomerDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.findById(id));
    }

    @Test
    @DisplayName("Should return paged customers")
    void shouldReturnPagedCustomers() {

        Customer customer = buildCustomer();

        Page<Customer> page = new PageImpl<>(List.of(customer));

        when(customerRepository.findAll(any(Pageable.class))).thenReturn(page);

        when(customerMapper.toResponse(customer)).thenReturn(buildResponse(customer));

        Page<CustomerResponse> result = customerService.findAll(PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should update customer")
    void shouldUpdateCustomer() {

        UUID id = UUID.randomUUID();

        Customer customer = buildCustomer();

        UpdateCustomerRequest request =
        new UpdateCustomerRequest(
                "Updated",
                "Customer"
        );

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        when(customerRepository.save(customer)).thenReturn(customer);

        when(customerMapper.toResponse(customer)).thenReturn(buildResponse(customer));

        CustomerResponse response = customerService.update(id, request);

        assertNotNull(response);

        verify(customerMapper).updateEntity(request, customer);

        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("Should delete customer")
    void shouldDeleteCustomer() {

        UUID id = UUID.randomUUID();

        Customer customer = buildCustomer();

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        customerService.delete(id);

        verify(customerRepository).delete(customer);
    }

    @Test
    @DisplayName("Should search customers")
    void shouldSearchCustomers() {

        Customer customer = buildCustomer();

        Page<Customer> page = new PageImpl<>(List.of(customer));

        when(customerRepository.findAll(
                any(Specification.class),
                any(Pageable.class))
        ).thenReturn(page);

        when(customerMapper.toResponse(customer)).thenReturn(buildResponse(customer));

        Page<CustomerResponse> result =
        customerService.search(
                "Ulisses",
                null,
                PageRequest.of(0, 20)
        );

        assertEquals(1, result.getContent().size());
    }

    private CreateCustomerRequest buildCreateRequest() {

        return new CreateCustomerRequest(
                "Ulisses",
                "Alves",
                "user@example.com"
        );
    }

    private Customer buildCustomer() {

        Customer customer = new Customer();

        customer.setId(UUID.randomUUID());

        customer.setFirstName("Ulisses");
        customer.setLastName("Alves");
        customer.setEmail("user@example.com");
        customer.setStatus(CustomerStatus.ACTIVE);

        return customer;
    }

    private CustomerResponse buildResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getStatus(),
                Instant.now(),
                Instant.now()
        );
    }

}
