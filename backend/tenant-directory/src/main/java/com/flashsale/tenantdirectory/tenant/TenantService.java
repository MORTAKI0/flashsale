package com.flashsale.tenantdirectory.tenant;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantService {
  private final TenantRepository tenantRepository;

  public TenantService(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Transactional(readOnly = true)
  public TenantEntity getByOrgIdOrThrow(String orgId) {
    return tenantRepository.findByOrgId(orgId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
  }

  @Transactional
  public TenantEntity upsert(String orgId, String tenantName) {
    TenantEntity entity = tenantRepository.findByOrgId(orgId)
        .orElseGet(() -> {
          TenantEntity fresh = new TenantEntity();
          fresh.setId(UUID.randomUUID());
          fresh.setOrgId(orgId);
          fresh.setRealm(orgId);
          fresh.setActive(true);
          return fresh;
        });

    entity.setTenantName(tenantName);
    if (entity.getRealm() == null || entity.getRealm().isBlank()) {
      entity.setRealm(orgId);
    }

    return tenantRepository.save(entity);
  }
}
