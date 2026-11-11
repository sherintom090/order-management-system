package com.retail.platform.inventory.config;

import com.retail.platform.inventory.security.KeycloakRoleConverter;
import com.retail.platform.inventory.security.ServiceAccountAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, ServiceAccountAuthorization serviceAccountAuthorization) throws Exception {

        JwtAuthenticationConverter jwtConverter =
                new JwtAuthenticationConverter();

        jwtConverter.setJwtGrantedAuthoritiesConverter(
                new KeycloakRoleConverter()
        );

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/**"
                        ).permitAll()

                        // Internal reservation operations
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/inventory/reservations"
                        ).access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(
                                        serviceAccountAuthorization.canReserve(
                                                authentication.get()
                                        )
                                )
                        )

                        // Existing inventory management APIs
                        .requestMatchers(
                                "/api/v1/inventory",
                                "/api/v1/inventory/**"
                        ).hasAnyRole("INVENTORY_MANAGER", "ADMIN")

                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                )
                .build();
    }
}