package dev.ulisses.highperformanceapi.application.exception;

public record FieldValidationError(

        String field,
        String message

) {
}
