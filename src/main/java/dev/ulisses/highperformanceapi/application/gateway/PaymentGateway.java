package dev.ulisses.highperformanceapi.application.gateway;

import dev.ulisses.highperformanceapi.application.dto.request.PaymentGatewayRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentGatewayResponse;

public interface PaymentGateway {

    PaymentGatewayResponse process(
            PaymentGatewayRequest request
    );

}
