package dev.ulisses.highperformanceapi.application.mapper;

import dev.ulisses.highperformanceapi.application.dto.request.CreateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateCustomerRequest;
import dev.ulisses.highperformanceapi.application.dto.response.CustomerResponse;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "status", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    List<CustomerResponse> toResponseList(List<Customer> customers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(
            UpdateCustomerRequest request,
            @MappingTarget Customer customer
    );
}
