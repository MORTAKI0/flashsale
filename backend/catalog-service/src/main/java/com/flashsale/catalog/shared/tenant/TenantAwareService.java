package com.flashsale.catalog.shared.tenant;

import java.util.List;
import java.util.Optional;

public abstract class TenantAwareService<T extends TenantOwnedEntity, ID> {

  private final TenantContextAccessor tenantContextAccessor;

  protected TenantAwareService(TenantContextAccessor tenantContextAccessor) {
    this.tenantContextAccessor = tenantContextAccessor;
  }

  protected abstract TenantAwareRepository<T, ID> repository();

  protected final String requiredTenantId() {
    return tenantContextAccessor.requiredTenantId();
  }

  protected final List<T> findAllForCurrentTenant() {
    return repository().findAllByTenantId(requiredTenantId());
  }

  protected final Optional<T> findByIdForCurrentTenant(ID id) {
    return repository().findByTenantIdAndId(requiredTenantId(), id);
  }

  protected final boolean existsByIdForCurrentTenant(ID id) {
    return repository().existsByTenantIdAndId(requiredTenantId(), id);
  }

  protected final T createForCurrentTenant(T entity) {
    return repository().createForTenant(requiredTenantId(), entity);
  }

  protected final T updateForCurrentTenant(T entity) {
    return repository().updateForTenant(requiredTenantId(), entity);
  }

  protected final boolean deleteByIdForCurrentTenant(ID id) {
    return repository().deleteByTenantIdAndId(requiredTenantId(), id) > 0L;
  }
}
