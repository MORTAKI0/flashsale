package com.flashsale.catalog.catalog.api;

import com.flashsale.catalog.catalog.dto.TenantContextResponse;
import com.flashsale.catalog.shared.tenant.TenantContext;
import com.flashsale.catalog.shared.tenant.TenantContextAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/context")
public class TenantContextController {

  private final TenantContextAccessor tenantContextAccessor;

  public TenantContextController(TenantContextAccessor tenantContextAccessor) {
    this.tenantContextAccessor = tenantContextAccessor;
  }

  @GetMapping("/whoami")
  public TenantContextResponse whoAmI() {
    TenantContext context = tenantContextAccessor.current()
        .orElseThrow(() -> new IllegalStateException("Tenant context is not available"));

    return new TenantContextResponse(
        context.tenantId(),
        context.userId(),
        context.roles(),
        context.correlationId()
    );
  }
}
