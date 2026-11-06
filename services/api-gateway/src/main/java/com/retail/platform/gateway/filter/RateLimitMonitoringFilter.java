package com.retail.platform.gateway.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RateLimitMonitoringFilter
        extends AbstractGatewayFilterFactory<
        RateLimitMonitoringFilter.Config> {

    private final Counter failOpenCounter;

    public RateLimitMonitoringFilter(MeterRegistry registry) {
        super(Config.class);

        this.failOpenCounter = Counter.builder(
                        "gateway.ratelimit.failopen")
                .description(
                        "Requests allowed when rate limiting fails open")
                .register(registry);
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {

                    String remaining = exchange.getResponse()
                            .getHeaders()
                            .getFirst("X-RateLimit-Remaining");

                    if ("-1".equals(remaining)) {
                        failOpenCounter.increment();
                    }
                }));
    }

    public static class Config {
    }
}