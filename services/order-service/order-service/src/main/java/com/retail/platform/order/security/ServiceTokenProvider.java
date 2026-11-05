package com.retail.platform.order.security;

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

@Component
public class ServiceTokenProvider {

    private static final String CLIENT_REGISTRATION = "inventory-service";
    private static final String SERVICE_PRINCIPAL = "order-service";

    private final OAuth2AuthorizedClientManager clientManager;

    public ServiceTokenProvider(OAuth2AuthorizedClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public String getAccessToken() {

        OAuth2AuthorizeRequest request =
                OAuth2AuthorizeRequest
                        .withClientRegistrationId(CLIENT_REGISTRATION)
                        .principal(SERVICE_PRINCIPAL)
                        .build();

        OAuth2AuthorizedClient authorizedClient =
                clientManager.authorize(request);

        if (authorizedClient == null ||
                authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException(
                    "Unable to obtain service access token"
            );
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }
}