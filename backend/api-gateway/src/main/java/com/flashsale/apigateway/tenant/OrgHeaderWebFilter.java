package com.flashsale.apigateway.tenant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Component
public class OrgHeaderWebFilter implements WebFilter, Ordered {

  private static final String ORG_HEADER = "X-ORG-ID";
  private static final String CORRELATION_HEADER = "X-CORRELATION-ID";
  private static final String ACTUATOR_PREFIX = "/actuator";
  private static final String API_PREFIX = "/api/";
  private static final String PUBLIC_API_PREFIX = "/api/public/";

  private final ObjectMapper objectMapper;

  public OrgHeaderWebFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String incomingCorrelationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_HEADER);
    String correlationId = StringUtils.hasText(incomingCorrelationId)
        ? incomingCorrelationId
        : UUID.randomUUID().toString();

    ServerHttpRequest request = exchange.getRequest();
    if (!StringUtils.hasText(incomingCorrelationId)) {
      request = request.mutate().header(CORRELATION_HEADER, correlationId).build();
    }

    ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();
    mutatedExchange.getResponse().getHeaders().set(CORRELATION_HEADER, correlationId);

    String path = mutatedExchange.getRequest().getPath().value();
    if (!isProtectedApiPath(path)) {
      return chain.filter(mutatedExchange);
    }

    String orgHeader = mutatedExchange.getRequest().getHeaders().getFirst(ORG_HEADER);
    if (!StringUtils.hasText(orgHeader)) {
      return writeJsonError(
          mutatedExchange,
          HttpStatus.BAD_REQUEST,
          "ORG_REQUIRED",
          "X-ORG-ID header is required",
          correlationId
      );
    }

    return ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication())
        .switchIfEmpty(Mono.justOrEmpty((Authentication) null))
        .flatMap(auth -> {
          if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return writeJsonError(
                mutatedExchange,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Missing or invalid JWT",
                correlationId
            );
          }

          Set<String> allowedOrgIds = resolveAllowedOrgIds(jwtAuth);

          if (!allowedOrgIds.contains(orgHeader)) {
            return writeJsonError(
                mutatedExchange,
                HttpStatus.FORBIDDEN,
                "ORG_FORBIDDEN",
                "X-ORG-ID is not allowed for this user",
                correlationId
            );
          }

          return chain.filter(mutatedExchange);
        });
  }

  private boolean isProtectedApiPath(String path) {
    return path.startsWith(API_PREFIX)
        && !path.startsWith(PUBLIC_API_PREFIX)
        && !path.startsWith(ACTUATOR_PREFIX);
  }

  private Set<String> resolveAllowedOrgIds(JwtAuthenticationToken jwtAuth) {
    Object orgIdsClaim = jwtAuth.getToken().getClaims().get("org_ids");
    Set<String> allowed = new LinkedHashSet<>();

    if (orgIdsClaim instanceof String value && StringUtils.hasText(value)) {
      allowed.add(value.trim());
      return allowed;
    }

    if (orgIdsClaim instanceof Collection<?> values) {
      values.stream()
          .filter(value -> value instanceof String)
          .map(value -> ((String) value).trim())
          .filter(StringUtils::hasText)
          .forEach(allowed::add);
      return allowed;
    }

    if (orgIdsClaim instanceof String[] arrayValues) {
      Arrays.stream(arrayValues)
          .map(String::trim)
          .filter(StringUtils::hasText)
          .forEach(allowed::add);
    }

    return allowed;
  }

  private Mono<Void> writeJsonError(
      ServerWebExchange exchange,
      HttpStatus status,
      String code,
      String message,
      String correlationId
  ) {
    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> payload = Map.of(
        "code", code,
        "message", message,
        "correlationId", correlationId,
        "path", exchange.getRequest().getPath().value(),
        "timestamp", Instant.now().toString()
    );

    byte[] bytes;
    try {
      bytes = objectMapper.writeValueAsBytes(payload);
    } catch (JsonProcessingException ex) {
      String fallback = "{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}";
      bytes = fallback.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    var buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }
}
