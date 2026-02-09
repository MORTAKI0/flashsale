package com.flashsale.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(ex -> ex
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/api/public/**").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/catalog/products/**").hasAnyRole("CLIENT", "OWNER")
            .pathMatchers(HttpMethod.POST, "/api/catalog/products/**").hasRole("OWNER")
            .pathMatchers(HttpMethod.PUT, "/api/catalog/products/**").hasRole("OWNER")
            .pathMatchers(HttpMethod.DELETE, "/api/catalog/products/**").hasRole("OWNER")
            .pathMatchers("/api/**").authenticated()
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
            jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
        ))
        .build();
  }

  @Bean
  Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
    delegate.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter());
    return new ReactiveJwtAuthenticationConverterAdapter(delegate);
  }
}

