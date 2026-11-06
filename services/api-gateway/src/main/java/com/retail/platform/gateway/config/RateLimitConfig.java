package com.retail.platform.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userKeyResolver() {

        return exchange -> exchange.getPrincipal()
                .filter(principal ->
                        principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(authentication ->
                        "user:" + authentication.getToken()
                                .getSubject());
    }
}