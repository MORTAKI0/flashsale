package com.flashsale.catalog.shared.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.catalog.shared.error.ApiErrorResponse;
import com.flashsale.catalog.shared.web.RequestMdcFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantEnforcementFilter extends OncePerRequestFilter {

  private static final String API_PREFIX = "/api";
  private static final String ORG_HEADER = "X-ORG-ID";

  private final ObjectMapper objectMapper;

  public TenantEnforcementFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return !(path.equals(API_PREFIX) || path.startsWith(API_PREFIX + "/"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      String orgId = request.getHeader(ORG_HEADER);
      if (!StringUtils.hasText(orgId)) {
        writeError(response, request, HttpStatus.BAD_REQUEST, "ORG_REQUIRED", "X-ORG-ID header is required");
        return;
      }

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (!(authentication instanceof JwtAuthenticationToken jwtToken) || !authentication.isAuthenticated()) {
        writeError(response, request, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid JWT");
        return;
      }

      Jwt jwt = jwtToken.getToken();
      List<String> allowedOrgs = extractOrgIds(jwt);
      String tenantId = orgId.trim();

      if (!allowedOrgs.contains(tenantId)) {
        writeError(response, request, HttpStatus.FORBIDDEN, "ORG_FORBIDDEN", "X-ORG-ID is not allowed for this user");
        return;
      }

      String userId = firstNonBlank(
          jwt.getClaimAsString("preferred_username"),
          jwt.getClaimAsString("sub"),
          authentication.getName()
      );

      List<String> roles = authentication.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .sorted()
          .toList();

      String correlationId = Objects.toString(
          request.getAttribute(RequestMdcFilter.ATTR_CORRELATION_ID),
          MDC.get("correlationId")
      );

      TenantContext context = new TenantContext(tenantId, userId, roles, correlationId);
      TenantContextHolder.set(context);

      MDC.put("tenantId", tenantId);
      MDC.put("userId", userId);

      filterChain.doFilter(request, response);
    } finally {
      TenantContextHolder.clear();
    }
  }

  private List<String> extractOrgIds(Jwt jwt) {
    Object orgIdsClaim = jwt.getClaims().get("org_ids");

    if (orgIdsClaim instanceof Collection<?> values) {
      return values.stream()
          .map(value -> Objects.toString(value, "").trim())
          .filter(StringUtils::hasText)
          .distinct()
          .toList();
    }

    if (orgIdsClaim instanceof String value && StringUtils.hasText(value)) {
      return List.of(value.trim());
    }

    return List.of();
  }

  private String firstNonBlank(String... candidates) {
    for (String candidate : candidates) {
      if (StringUtils.hasText(candidate)) {
        return candidate.trim();
      }
    }
    return "unknown";
  }

  private void writeError(
      HttpServletResponse response,
      HttpServletRequest request,
      HttpStatus status,
      String code,
      String message
  ) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    String correlationId = Objects.toString(
        request.getAttribute(RequestMdcFilter.ATTR_CORRELATION_ID),
        MDC.get("correlationId")
    );

    ApiErrorResponse body = new ApiErrorResponse(
        code,
        message,
        correlationId,
        request.getRequestURI(),
        OffsetDateTime.now()
    );

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
