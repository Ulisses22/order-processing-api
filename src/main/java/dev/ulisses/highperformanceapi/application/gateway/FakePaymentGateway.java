package dev.ulisses.highperformanceapi.application.gateway;

import dev.ulisses.highperformanceapi.application.dto.request.PaymentGatewayRequest;
import dev.ulisses.highperformanceapi.application.dto.response.PaymentGatewayResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FakePaymentGateway implements PaymentGateway {

    private final boolean forceFailure;

    public FakePaymentGateway() {
        this(false);
    }

    FakePaymentGateway(boolean forceFailure) {
        this.forceFailure = forceFailure;
    }

    @Override
    public PaymentGatewayResponse process(
            PaymentGatewayRequest request
    ) {

        if (forceFailure) {
            return new PaymentGatewayResponse(
                    false,
                    null,
                    null,
                    "Payment declined by fake gateway"
            );
        }

        return new PaymentGatewayResponse(
                true,
                "FAKE-TXN-" + UUID.randomUUID(),
                "FAKE-AUTH-" + UUID.randomUUID(),
                null
        );
    }
}
