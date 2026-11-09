package com.retail.platform.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.platform.gateway.error.ApiErrorResponse;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class RateLimitErrorResponseFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    public RateLimitErrorResponseFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        return chain.filter(exchange).then(Mono.defer(() -> {

            var response = exchange.getResponse();

            if (response.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS
                    || response.isCommitted()) {
                return Mono.empty();
            }

            ApiErrorResponse error = new ApiErrorResponse(
                    429,
                    "Too Many Requests",
                    "API rate limit exceeded. Please try again later.",
                    exchange.getRequest().getPath().value(),
                    exchange.getRequest().getHeaders()
                            .getFirst("X-Correlation-ID"),
                    Instant.now()
            );

            try {
                byte[] body = objectMapper.writeValueAsBytes(error);

                response.getHeaders().setContentType(
                        MediaType.APPLICATION_JSON);
                response.getHeaders().setContentLength(body.length);

                return response.writeWith(
                        Mono.just(response.bufferFactory().wrap(body))
                );
            } catch (Exception ex) {
                return Mono.error(ex);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -101;
    }
}