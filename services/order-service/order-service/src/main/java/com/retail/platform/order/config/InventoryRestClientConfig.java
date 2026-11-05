package com.retail.platform.order.config;

import com.retail.platform.order.security.ServiceTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class InventoryRestClientConfig {

    @Bean
    public RestClient inventoryRestClient(
            RestClient.Builder builder,
            ServiceTokenProvider tokenProvider,
            @Value("${inventory.service.base-url}") String baseUrl) {

        return builder
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {

                    String accessToken = tokenProvider.getAccessToken();

                    request.getHeaders()
                            .setBearerAuth(accessToken);

                    return execution.execute(request, body);
                })
                .requestFactory(
                        new org.springframework.http.client
                                .SimpleClientHttpRequestFactory() {{
                            setConnectTimeout(Duration.ofSeconds(3));
                            setReadTimeout(Duration.ofSeconds(5));
                        }}
                )
                .build();
    }
}