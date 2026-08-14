package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.CreatePaymentRequest;
import dev.ulisses.highperformanceapi.application.dto.request.PaymentSearchRequest;
import dev.ulisses.highperformanceapi.application.dto.request.UpdatePaymentStatusRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentResponse;
import dev.ulisses.highperformanceapi.application.service.PaymentService;
import dev.ulisses.highperformanceapi.domain.enums.PaymentMethod;
import dev.ulisses.highperformanceapi.domain.enums.PaymentStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

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

    @PatchMapping("/{id}/status")
    public PaymentResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentStatusRequest request
    ) {

        return paymentService.updateStatus(
                id,
                request.status()
        );
    }

    @GetMapping
    public Page<PaymentResponse> search(

            @RequestParam(required = false) UUID orderId,

            @RequestParam(required = false) PaymentStatus status,

            @RequestParam(required = false) PaymentMethod paymentMethod,

            @RequestParam(required = false) String transactionId,

            @RequestParam(required = false) Instant createdFrom,

            @RequestParam(required = false) Instant createdTo,

            Pageable pageable
    ) {

        PaymentSearchRequest request = new PaymentSearchRequest(
                orderId,
                status,
                paymentMethod,
                transactionId,
                createdFrom,
                createdTo
        );

        return paymentService.search(request, pageable);
    }
}
