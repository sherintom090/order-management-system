package com.retail.platform.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        return Mono.defer(() -> {

            long startTime = System.nanoTime();

            String correlationId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Correlation-ID");

            String method = exchange.getRequest()
                    .getMethod()
                    .name();

            String path = exchange.getRequest()
                    .getPath()
                    .value();

            return chain.filter(exchange)
                    .doFinally(signalType -> {

                        long durationMs =
                                TimeUnit.NANOSECONDS.toMillis(
                                        System.nanoTime() - startTime);

                        HttpStatusCode status =
                                exchange.getResponse().getStatusCode();

                        int statusCode =
                                status != null ? status.value() : 0;

                        log.info(
                                "gateway_request method={} path={} status={} durationMs={} correlationId={} signal={}",
                                method,
                                path,
                                statusCode,
                                durationMs,
                                correlationId,
                                signalType
                        );
                    });
        });
    }

    @Override
    public int getOrder() {
        return -99;
    }
}