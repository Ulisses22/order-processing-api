package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreatePaymentRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdateOrderStatusRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse create(CreatePaymentRequest request);

    PaymentResponse updateStatus(UUID id, PaymentStatus status);

}