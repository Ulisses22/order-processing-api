package dev.ulisses.highperformanceapi.application.exception;

import java.time.Instant;
import java.util.List;

public record ValidationErrorResponse(

        Instant timestamp,
        int status,
        String error,
        String path,
        List<FieldValidationError> fieldErrors

) {
}
