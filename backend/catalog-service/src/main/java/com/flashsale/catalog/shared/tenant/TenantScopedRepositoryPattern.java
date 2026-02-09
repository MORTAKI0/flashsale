package com.flashsale.catalog.shared.tenant;

/**
 * Base helper contract for tenant-scoped repository operations.
 */
public interface TenantScopedRepositoryPattern {

  default String requireTenantId(String tenantId) {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("tenantId is required for repository operations");
    }
    return tenantId.trim();
  }

  default void requireTenantMatch(String tenantId, String entityTenantId) {
    String requiredTenantId = requireTenantId(tenantId);
    String requiredEntityTenantId = requireTenantId(entityTenantId);
    if (!requiredTenantId.equals(requiredEntityTenantId)) {
      throw new IllegalArgumentException("Cross-tenant write is not allowed");
    }
  }
}
