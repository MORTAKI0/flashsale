package com.flashsale.catalog.shared.tenant;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TenantContextAccessor {

  public Optional<TenantContext> current() {
    return Optional.ofNullable(TenantContextHolder.get());
  }

  public String requiredTenantId() {
    TenantContext context = TenantContextHolder.get();
    if (context == null || context.tenantId() == null || context.tenantId().isBlank()) {
      throw new IllegalStateException("Tenant context is not available");
    }
    return context.tenantId();
  }

  public Optional<String> currentUserId() {
    return current().map(TenantContext::userId);
  }

  public Optional<String> currentCorrelationId() {
    return current().map(TenantContext::correlationId);
  }
}
