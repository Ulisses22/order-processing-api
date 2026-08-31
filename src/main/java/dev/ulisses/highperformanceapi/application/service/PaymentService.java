package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreatePaymentRequest;
import dev.ulisses.highperformanceapi.application.dto.request.PaymentSearchRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateOrderStatusRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse create(CreatePaymentRequest request);

    PaymentResponse updateStatus(UUID id, PaymentStatus status);

    Page<PaymentResponse> search(
            PaymentSearchRequest request,
            @ParameterObject Pageable pageable
    );

}