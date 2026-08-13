package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.dto.request.CreatePaymentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse create(CreatePaymentRequest request);

}