package com.retail.platform.gateway.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RateLimitMonitoringFilter
        extends AbstractGatewayFilterFactory<
        RateLimitMonitoringFilter.Config> {

    private static final Logger log =
            LoggerFactory.getLogger(RateLimitMonitoringFilter.class);

    private final Counter suspectedFailOpenCounter;

    public RateLimitMonitoringFilter(MeterRegistry registry) {
        super(Config.class);

        this.suspectedFailOpenCounter = Counter.builder(
                        "gateway.ratelimit.suspected.failopen")
                .description(
                        "Potential rate limiter fail-open events inferred from response headers")
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

                        suspectedFailOpenCounter.increment();

                        log.warn(
                                "Potential rate limiter fail-open detected. path={}",
                                exchange.getRequest().getPath());
                    }
                }));
    }

    public static class Config {
    }
}