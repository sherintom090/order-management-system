package com.retail.platform.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.platform.gateway.error.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class JsonAccessDeniedHandler
        implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            AccessDeniedException exception) {

        var response = exchange.getResponse();

        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiErrorResponse error = new ApiErrorResponse(
                403,
                "Forbidden",
                "You do not have permission to access this resource",
                exchange.getRequest().getPath().value(),
                exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-Correlation-ID"),
                Instant.now()
        );

        return Mono.defer(() -> {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(error);

                return response.writeWith(
                        Mono.just(response.bufferFactory().wrap(bytes))
                );
            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }
        });
    }
}