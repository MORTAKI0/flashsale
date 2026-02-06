package com.flashsale.catalog.shared.tenant;

/**
 * Lightweight repository pattern for tenant-safe persistence.
 * Every repository method must require tenantId as an explicit parameter.
 */
public interface TenantScopedRepositoryPattern {

  default String requireTenantId(String tenantId) {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("tenantId is required for repository operations");
    }
    return tenantId;
  }
}
