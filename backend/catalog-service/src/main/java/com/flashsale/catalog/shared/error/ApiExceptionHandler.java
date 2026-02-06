package com.flashsale.catalog.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(this::fieldMessage)
        .orElse("Validation failed");

    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", request);
  }

  private ResponseEntity<ApiErrorResponse> build(
      HttpStatus status,
      String code,
      String message,
      HttpServletRequest request
  ) {
    ApiErrorResponse response = new ApiErrorResponse(
        code,
        message,
        MDC.get("correlationId"),
        request.getRequestURI(),
        OffsetDateTime.now()
    );

    return ResponseEntity.status(status).body(response);
  }

  private String fieldMessage(FieldError error) {
    return error.getField() + " " + error.getDefaultMessage();
  }
}
