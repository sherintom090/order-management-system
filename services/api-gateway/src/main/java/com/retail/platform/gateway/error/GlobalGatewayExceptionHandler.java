package com.retail.platform.gateway.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Order(-2)
public class GlobalGatewayExceptionHandler
        implements WebExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalGatewayExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalGatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable exception) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Correlation-ID");

        log.error(
                "Unexpected Gateway error. path={}, correlationId={}",
                exchange.getRequest().getPath(),
                correlationId,
                exception
        );

        ApiErrorResponse error = new ApiErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred while processing the request",
                exchange.getRequest().getPath().value(),
                correlationId,
                Instant.now()
        );

        return Mono.fromCallable(() ->
                        objectMapper.writeValueAsBytes(error))
                .flatMap(bytes -> {
                    var response = exchange.getResponse();

                    response.setStatusCode(
                            HttpStatus.INTERNAL_SERVER_ERROR);
                    response.getHeaders().setContentType(
                            MediaType.APPLICATION_JSON);
                    response.getHeaders().setContentLength(bytes.length);

                    return response.writeWith(
                            Mono.just(response.bufferFactory().wrap(bytes))
                    );
                });
    }
}