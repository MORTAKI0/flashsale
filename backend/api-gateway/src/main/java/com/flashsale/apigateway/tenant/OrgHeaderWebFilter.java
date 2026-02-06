package com.flashsale.apigateway.tenant;

import java.nio.charset.StandardCharsets;
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
import reactor.core.publisher.Mono;

@Component
public class OrgHeaderWebFilter implements WebFilter, Ordered {

  private static final String ORG_HEADER = "X-ORG-ID";
  private static final String ACTUATOR_PREFIX = "/actuator";

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    if (path.startsWith(ACTUATOR_PREFIX)) {
      return chain.filter(exchange);
    }

    String orgHeader = exchange.getRequest().getHeaders().getFirst(ORG_HEADER);
    if (!StringUtils.hasText(orgHeader)) {
      return writePlainText(exchange, HttpStatus.BAD_REQUEST, "Missing X-ORG-ID");
    }

    return ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication())
        .switchIfEmpty(Mono.justOrEmpty((Authentication) null))
        .flatMap(auth -> {
          if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return writePlainText(exchange, HttpStatus.UNAUTHORIZED, "Missing/invalid token");
          }

          String username = jwtAuth.getToken().getClaimAsString("preferred_username");
          if (!StringUtils.hasText(username)) {
            return writePlainText(exchange, HttpStatus.FORBIDDEN, "Org claim missing");
          }

          if (!orgHeader.equals(username)) {
            return writePlainText(exchange, HttpStatus.FORBIDDEN, "X-ORG-ID does not match token");
          }

          return chain.filter(exchange);
        });
  }

  private Mono<Void> writePlainText(ServerWebExchange exchange, HttpStatus status, String message) {
    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
    byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
    var buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }
}
