package dev.ulisses.highperformanceapi.application.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<FieldValidationError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .toList();

        ValidationErrorResponse response = buildValidationErrorResponse(
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(
                        HttpStatus.CONFLICT,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest()
                .body(buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(
                        HttpStatus.CONFLICT,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // TODO: Replace with SLF4J logging
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred.",
                        request.getRequestURI()
                ));
    }

    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String message,
            String path) {

        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }

    private ValidationErrorResponse buildValidationErrorResponse(
            HttpStatus status,
            String path,
            List<FieldValidationError> fieldErrors) {

        return new ValidationErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                path,
                fieldErrors
        );
    }

    private FieldValidationError mapFieldError(FieldError error) {
        return new FieldValidationError(
                error.getField(),
                error.getDefaultMessage()
        );
    }
}
