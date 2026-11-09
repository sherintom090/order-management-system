package com.retail.platform.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.platform.gateway.error.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class JsonAuthenticationEntryPoint
        implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException exception) {

        var response = exchange.getResponse();

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(
                "WWW-Authenticate",
                "Bearer"
        );

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Correlation-ID");

        ApiErrorResponse error = new ApiErrorResponse(
                401,
                "Unauthorized",
                "Authentication is required or the access token is invalid",
                exchange.getRequest().getPath().value(),
                correlationId,
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