package dev.ulisses.highperformanceapi.application.dto.response;

public record PaymentGatewayResponse(

        boolean successful,

        String transactionId,

        String authorizationCode,

        String failureReason

) {
}
