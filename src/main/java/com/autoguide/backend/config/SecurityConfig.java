package com.autoguide.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    private final boolean securityEnabled;
    private final String jwkSetUri;
    private final Set<String> acceptedIssuers;

    public SecurityConfig(
            @Value("${app.security.enabled:true}") boolean securityEnabled,
            @Value("${app.security.jwk-set-uri:http://localhost:8081/realms/autoguide/protocol/openid-connect/certs}") String jwkSetUri,
            @Value("${app.security.accepted-issuers:http://localhost:8081/realms/autoguide,http://keycloak:8080/realms/autoguide,http://autoguide-keycloak:8080/realms/autoguide,http://localhost:8080/realms/autoguide}") String acceptedIssuersRaw
    ) {
        this.securityEnabled = securityEnabled;
        this.jwkSetUri = jwkSetUri;
        this.acceptedIssuers = Arrays.stream(acceptedIssuersRaw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesConverter,
            ReactiveJwtDecoder jwtDecoder
    ) {
        if (!securityEnabled) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .build();
        }

        Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter =
                jwt -> Mono.just(new JwtAuthenticationToken(
                        jwt,
                        grantedAuthoritiesConverter.convert(jwt),
                        resolvePrincipalName(jwt)
                ));

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/prometheus",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/swagger-ui/**",
                                "/assets/**",
                                "/uploads/**",
                                "/api/v1/public/**"
                        ).permitAll()
                        .pathMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .pathMatchers("/api/v1/**").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .jwtDecoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = jwt -> {
            String issuer = jwt.getIssuer() == null ? "" : jwt.getIssuer().toString();
            if (acceptedIssuers.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }

            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "Token issuer is not allowed: " + issuer,
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        };

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, issuerValidator));
        return jwtDecoder;
    }

    private String resolvePrincipalName(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        String username = jwt.getClaimAsString("username");
        if (username != null && !username.isBlank()) {
            return username;
        }

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        return jwt.getSubject();
    }
}
