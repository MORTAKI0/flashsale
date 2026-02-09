package com.flashsale.catalog.shared.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Set<String> authorities = new LinkedHashSet<>();

    Collection<GrantedAuthority> defaults = defaultConverter.convert(jwt);
    if (defaults != null) {
      defaults.stream().map(GrantedAuthority::getAuthority).forEach(authorities::add);
    }

    authorities.addAll(realmRoles(jwt));

    return authorities.stream()
        .map(SimpleGrantedAuthority::new)
        .map(GrantedAuthority.class::cast)
        .toList();
  }

  private List<String> realmRoles(Jwt jwt) {
    Object realmAccess = jwt.getClaims().get("realm_access");
    if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
      return List.of();
    }

    Object rolesObject = realmAccessMap.get("roles");
    if (!(rolesObject instanceof Collection<?> roles)) {
      return List.of();
    }

    List<String> mappedRoles = new ArrayList<>();
    for (Object role : roles) {
      String value = Objects.toString(role, "").trim();
      if (!value.isEmpty()) {
        mappedRoles.add("ROLE_" + value.toUpperCase());
      }
    }

    return mappedRoles;
  }
}
