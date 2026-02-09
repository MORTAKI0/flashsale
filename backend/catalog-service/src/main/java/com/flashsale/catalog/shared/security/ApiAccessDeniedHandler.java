package com.flashsale.catalog.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.catalog.shared.error.ApiErrorResponse;
import com.flashsale.catalog.shared.web.RequestMdcFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException
  ) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    String correlationId = Objects.toString(
        request.getAttribute(RequestMdcFilter.ATTR_CORRELATION_ID),
        MDC.get("correlationId")
    );

    ApiErrorResponse body = new ApiErrorResponse(
        "FORBIDDEN",
        "Access denied",
        correlationId,
        request.getRequestURI(),
        OffsetDateTime.now()
    );

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
