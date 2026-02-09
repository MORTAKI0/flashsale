package com.flashsale.catalog.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ProductEntityId implements Serializable {

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;
}
