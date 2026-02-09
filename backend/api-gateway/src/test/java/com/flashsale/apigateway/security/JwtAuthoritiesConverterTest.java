package com.flashsale.apigateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthoritiesConverterTest {

  private final JwtAuthoritiesConverter converter = new JwtAuthoritiesConverter();

  @Test
  void shouldMapRealmRolesAlongsideScopes() {
    Jwt jwt = new Jwt(
        "token-value",
        Instant.now(),
        Instant.now().plusSeconds(600),
        Map.of("alg", "none"),
        Map.of(
            "scope", "openid profile email",
            "realm_access", Map.of("roles", List.of("OWNER", "CLIENT"))
        )
    );

    List<String> authorities = converter.convert(jwt).stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    assertThat(authorities).contains("ROLE_OWNER", "ROLE_CLIENT");
    assertThat(authorities).contains("SCOPE_openid", "SCOPE_profile", "SCOPE_email");
  }
}
