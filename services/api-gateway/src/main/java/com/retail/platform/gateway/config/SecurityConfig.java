package com.retail.platform.gateway.config;

import com.retail.platform.gateway.security.KeycloakRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        KeycloakRoleConverter roleConverter =
                new KeycloakRoleConverter();

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchange -> exchange

                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/v1/orders",
                                "/api/v1/orders/"
                        ).hasAnyRole("CUSTOMER", "ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/v1/orders/**"
                        ).hasAnyRole("CUSTOMER", "ADMIN")

                        .pathMatchers(
                                "/api/v1/inventory/**"
                        ).hasAnyRole("INVENTORY_MANAGER", "ADMIN")

                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(token -> {
                                    AbstractAuthenticationToken authentication =
                                            new JwtAuthenticationToken(
                                                    token,
                                                    roleConverter.convert(token)
                                            );
                                    return Mono.just(authentication);
                                })
                        )
                )
                .build();
    }
}