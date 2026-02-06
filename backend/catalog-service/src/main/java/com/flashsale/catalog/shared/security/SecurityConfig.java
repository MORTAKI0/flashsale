package com.flashsale.catalog.shared.security;

import com.flashsale.catalog.shared.tenant.TenantEnforcementFilter;
import com.flashsale.catalog.shared.web.RequestMdcFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      RequestMdcFilter requestMdcFilter,
      TenantEnforcementFilter tenantEnforcementFilter,
      ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
      ApiAccessDeniedHandler apiAccessDeniedHandler
  ) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(apiAuthenticationEntryPoint)
            .accessDeniedHandler(apiAccessDeniedHandler)
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
            jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
        ));

    http.addFilterBefore(requestMdcFilter, BearerTokenAuthenticationFilter.class);
    http.addFilterAfter(tenantEnforcementFilter, BearerTokenAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter());
    return converter;
  }
}
