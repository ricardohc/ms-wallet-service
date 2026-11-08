package com.banking.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Perfil dev: permite todo, sin JWT. Útil para Postman/curl local
     * mientras no exista un Identity Provider real (Keycloak/Okta).
     * NUNCA usar en producción.
     */
    @Bean
    @Profile("dev")
    public SecurityWebFilterChain devSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }

    /**
     * Perfil no-dev: OAuth2 Resource Server con JWT real.
     * El `phone_number` se toma del token emitido por el IdP.
     */
    @Bean
    @Profile("!dev")
    public SecurityWebFilterChain jwtSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> { }))
                .build();
    }
}