package com.retail.platform.inventory.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("serviceAccountAuthorization")
public class ServiceAccountAuthorization {

    public boolean canReserve(Authentication authentication) {

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return false;
        }

        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_INVENTORY_RESERVE"));

        boolean correctClient = "order-service-client"
                .equals(jwtAuth.getToken().getClaimAsString("azp"));

        return hasRole && correctClient;
    }
}