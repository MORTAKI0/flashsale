package com.flashsale.catalog.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestMdcFilter extends OncePerRequestFilter {

  public static final String CORRELATION_HEADER = "X-CORRELATION-ID";
  public static final String ATTR_CORRELATION_ID = "correlationId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String correlationId = request.getHeader(CORRELATION_HEADER);
    if (!StringUtils.hasText(correlationId)) {
      correlationId = UUID.randomUUID().toString();
    }

    String tenantId = request.getHeader("X-ORG-ID");

    MDC.put("correlationId", correlationId);
    MDC.put("tenantId", StringUtils.hasText(tenantId) ? tenantId.trim() : "unknown");
    MDC.put("userId", "unknown");

    request.setAttribute(ATTR_CORRELATION_ID, correlationId);
    response.setHeader(CORRELATION_HEADER, correlationId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove("tenantId");
      MDC.remove("correlationId");
      MDC.remove("userId");
    }
  }
}
