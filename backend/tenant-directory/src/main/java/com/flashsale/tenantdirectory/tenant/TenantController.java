package com.flashsale.tenantdirectory.tenant;

import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
  private final TenantService tenantService;

  public TenantController(TenantService tenantService) {
    this.tenantService = tenantService;
  }

  @GetMapping("/{orgId}")
  public TenantResponse getTenant(@PathVariable String orgId) {
    return TenantResponse.from(tenantService.getByOrgIdOrThrow(orgId));
  }

  @PostMapping
  public TenantResponse upsertTenant(@RequestBody TenantUpsertRequest request) {
    TenantEntity entity = tenantService.upsert(request.orgId(), request.tenantName());
    return TenantResponse.from(entity);
  }

  public record TenantUpsertRequest(String orgId, String tenantName) {
  }

  public record TenantResponse(UUID id, String orgId, String tenantName, boolean active,
                               Instant createdAt) {
    static TenantResponse from(TenantEntity entity) {
      return new TenantResponse(
          entity.getId(),
          entity.getOrgId(),
          entity.getTenantName(),
          entity.isActive(),
          entity.getCreatedAt()
      );
    }
  }
}
