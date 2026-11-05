package com.retail.platform.order.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.retail.platform.order.security.ServiceTokenProvider;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;

import java.util.UUID;

@SpringBootTest
class InventoryClientIntegrationTest {

    @Autowired
    private InventoryClient inventoryClient;

    @Test
    void shouldReserveInventoryUsingServiceAccount() {

        String orderId = "TEST-" + UUID.randomUUID();

        inventoryClient.reserveStock(
                orderId,
                "LAPTOP-001",
                1
        );
    }

    @Autowired
    private ServiceTokenProvider serviceTokenProvider;

    @Test
    void shouldReuseServiceAccessToken() {

        String firstToken = serviceTokenProvider.getAccessToken();
        String secondToken = serviceTokenProvider.getAccessToken();

        assertNotNull(firstToken);
        assertEquals(firstToken, secondToken);
    }

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void shouldAcquireNewTokenWhenCachedTokenIsExpired() {

        String registrationId = "inventory-service";
        String principalName = "order-service";

        // 1. Obtain a valid token
        String originalToken = serviceTokenProvider.getAccessToken();

        OAuth2AuthorizedClient existingClient =
                authorizedClientService.loadAuthorizedClient(
                        registrationId,
                        principalName
                );

        assertNotNull(existingClient);

        // 2. Simulate an expired token
        OAuth2AccessToken expiredToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                originalToken,
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(300)
        );

        // 3. Replace the cached token with the expired version
        OAuth2AuthorizedClient expiredClient =
                new OAuth2AuthorizedClient(
                        existingClient.getClientRegistration(),
                        principalName,
                        expiredToken
                );

        authorizedClientService.saveAuthorizedClient(
                expiredClient,
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(
                        principalName,
                        "N/A"
                )
        );

        // 4. Request a token again
        String renewedToken = serviceTokenProvider.getAccessToken();

        // 5. Verify Spring obtained a replacement
        assertNotNull(renewedToken);
        assertNotEquals(originalToken, renewedToken);
    }

    @Test
    void shouldFailWhenInventoryServiceIsUnavailable() {

        String orderId = "FAIL-" + UUID.randomUUID();

        InventoryServiceException exception = assertThrows(
                InventoryServiceException.class,
                () -> inventoryClient.reserveStock(
                        orderId,
                        "LAPTOP-001",
                        1
                )
        );

        assertEquals(503, exception.getStatusCode());
    }

    @Test
    void shouldFailWhenInventoryServiceTimesOut() {

        String orderId = "TIMEOUT-" + UUID.randomUUID();

        InventoryServiceException exception = assertThrows(
                InventoryServiceException.class,
                () -> inventoryClient.reserveStock(
                        orderId,
                        "LAPTOP-001",
                        1
                )
        );

        assertEquals(503, exception.getStatusCode());
    }
}

