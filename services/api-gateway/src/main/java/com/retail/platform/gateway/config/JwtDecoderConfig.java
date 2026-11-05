package com.retail.platform.gateway.config;

import com.retail.platform.gateway.security.AudienceValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            String issuerUri) {

        NimbusReactiveJwtDecoder decoder =
                (NimbusReactiveJwtDecoder)
                        ReactiveJwtDecoders.fromIssuerLocation(issuerUri);

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer(issuerUri),
                        new AudienceValidator()
                )
        );

        return decoder;
    }
}