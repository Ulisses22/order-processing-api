package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.CreateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.response.CustomerResponse;
import dev.ulisses.highperformanceapi.application.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        return customerService.create(request);
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(
            @PathVariable UUID id
    ) {
        return customerService.findById(id);
    }

    @GetMapping
    public Page<CustomerResponse> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @PageableDefault(
                    // page = 0,
                    size = 20,
                    sort = "createdAt"
            ) Pageable pageable
    ) {

        if (name != null || email != null) {
            return customerService.search(name, email, pageable);
        }

        return customerService.findAll(pageable);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        customerService.delete(id);
    }
}
