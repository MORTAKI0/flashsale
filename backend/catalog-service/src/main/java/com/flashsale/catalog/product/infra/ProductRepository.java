package com.flashsale.catalog.product.infra;

import com.flashsale.catalog.product.domain.ProductEntity;
import com.flashsale.catalog.product.domain.ProductEntityId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, ProductEntityId> {
    Page<ProductEntity> findByIdTenantIdAndActiveTrue(String tenantId, Pageable pageable);

    Optional<ProductEntity> findByIdTenantIdAndIdProductIdAndActiveTrue(String tenantId, UUID productId);

    @Query(value = """
        select p from ProductEntity p
        where p.id.tenantId = :tenantId
          and p.active = true
          and (lower(p.name) like lower(concat('%', :q, '%'))
               or lower(coalesce(p.description, '')) like lower(concat('%', :q, '%')))
    """)
    Page<ProductEntity> searchActiveByTenant(
        @Param("tenantId") String tenantId,
        @Param("q") String q,
        Pageable pageable
    );

}
