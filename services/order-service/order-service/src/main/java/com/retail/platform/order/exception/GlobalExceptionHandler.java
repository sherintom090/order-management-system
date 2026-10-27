package com.retail.platform.order.exception;

import com.retail.platform.order.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ApiErrorResponse handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest request) {

        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("ORDER_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiErrorResponse handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> errors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> ApiErrorResponse.FieldError.builder()
                                .field(error.getField())
                                .message(error.getDefaultMessage())
                                .build())
                        .toList();

        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ApiErrorResponse handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        return ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();
    }
}