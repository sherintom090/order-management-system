package com.retail.platform.gateway.controller;

import com.retail.platform.gateway.error.ApiErrorResponse;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import java.time.Instant;

@RestController
public class FallbackController {

    @GetMapping("/fallback/orders")
    public ResponseEntity<ApiErrorResponse> orderFallback(
            ServerWebExchange exchange) {

        return serviceUnavailable(
                exchange,
                "Order Service is temporarily unavailable"
        );
    }

    @GetMapping("/fallback/inventory")
    public ResponseEntity<ApiErrorResponse> inventoryFallback(
            ServerWebExchange exchange) {

        return serviceUnavailable(
                exchange,
                "Inventory Service is temporarily unavailable"
        );
    }

    private ResponseEntity<ApiErrorResponse> serviceUnavailable(
            ServerWebExchange exchange,
            String message) {

        var circuitBreakerException = exchange.getAttribute(
                ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR
        );

        if (circuitBreakerException == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Correlation-ID");

        String path = exchange.getRequest()
                .getPath()
                .value();

        var originalRequestUrl = exchange.getAttribute(
                ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR
        );

        if (originalRequestUrl instanceof java.util.Set<?> urls
                && !urls.isEmpty()) {

            Object firstUrl = urls.iterator().next();

            if (firstUrl instanceof java.net.URI uri) {
                path = uri.getPath();
            }
        }

        ApiErrorResponse response = new ApiErrorResponse(
                503,
                "Service Unavailable",
                message,
                path,
                correlationId,
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}