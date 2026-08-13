package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.CreatePaymentRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.application.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request
    ) {

        PaymentResponse response = paymentService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + response.id()))
                .body(response);
    }
}
