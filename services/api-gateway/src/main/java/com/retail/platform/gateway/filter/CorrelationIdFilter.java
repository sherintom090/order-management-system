package com.retail.platform.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(CorrelationIdFilter.class);

    public static final String CORRELATION_ID =
            "X-Correlation-ID";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String incomingId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID);

        String correlationId = isValid(incomingId)
                ? incomingId
                : UUID.randomUUID().toString();

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers ->
                        headers.set(CORRELATION_ID, correlationId))
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request)
                .build();

        mutatedExchange.getResponse().beforeCommit(() -> {
            mutatedExchange.getResponse()
                    .getHeaders()
                    .set(CORRELATION_ID, correlationId);

            return Mono.empty();
        });

        log.debug(
                "Gateway request: method={}, path={}, correlationId={}",
                request.getMethod(),
                request.getURI().getPath(),
                correlationId
        );

        return chain.filter(mutatedExchange);
    }

    private boolean isValid(String value) {
        return value != null
                && value.length() <= 128
                && value.matches("[a-zA-Z0-9._-]+");
    }

    @Override
    public int getOrder() {
        return -100;
    }
}