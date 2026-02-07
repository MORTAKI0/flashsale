package com.flashsale.catalog.shared.tenant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface TenantAwareRepository<T extends TenantOwnedEntity, ID>
    extends Repository<T, ID>, TenantScopedRepositoryPattern {

  Optional<T> findByTenantIdAndId(String tenantId, ID id);

  List<T> findAllByTenantId(String tenantId);

  boolean existsByTenantIdAndId(String tenantId, ID id);

  long deleteByTenantIdAndId(String tenantId, ID id);

  <S extends T> S save(S entity);

  default <S extends T> S createForTenant(String tenantId, S entity) {
    String requiredTenantId = requireTenantId(tenantId);
    entity.setTenantId(requiredTenantId);
    return save(entity);
  }

  default <S extends T> S updateForTenant(String tenantId, S entity) {
    requireTenantMatch(tenantId, entity.getTenantId());
    return save(entity);
  }
}
