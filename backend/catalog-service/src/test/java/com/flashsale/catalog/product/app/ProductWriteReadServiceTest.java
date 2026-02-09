package com.flashsale.catalog.product.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flashsale.catalog.product.domain.ProductEntity;
import com.flashsale.catalog.product.domain.ProductEntityId;
import com.flashsale.catalog.product.dto.PagedResponseDto;
import com.flashsale.catalog.product.dto.ProductSummaryDto;
import com.flashsale.catalog.product.infra.ProductRepository;
import com.flashsale.catalog.shared.error.NotFoundException;
import com.flashsale.catalog.shared.tenant.TenantContextAccessor;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductWriteReadServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TenantContextAccessor tenantContextAccessor;

    private ProductWriteService productWriteService;
    private ProductReadService productReadService;

    @BeforeEach
    void setUp() {
        productWriteService = new ProductWriteService(productRepository, tenantContextAccessor);
        productReadService = new ProductReadService(productRepository, tenantContextAccessor);
    }

    @Test
    void softDeleteThenGetAndListExcludeInactiveProduct() {
        String tenantId = "org-1";
        UUID productId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-02-09T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-02-09T10:00:00Z");

        ProductEntity entity = ProductEntity.builder()
            .id(new ProductEntityId(tenantId, productId))
            .name("Phone")
            .description("Demo")
            .priceCents(129900)
            .currency("USD")
            .active(true)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

        when(tenantContextAccessor.requiredTenantId()).thenReturn(tenantId);
        when(productRepository.findByIdTenantIdAndIdProductIdAndActiveTrue(tenantId, productId))
            .thenReturn(Optional.of(entity))
            .thenReturn(Optional.empty());
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findByIdTenantIdAndActiveTrue(eq(tenantId), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(productRepository.searchActiveByTenant(eq(tenantId), eq("Phone"), any(Pageable.class)))
            .thenReturn(Page.empty());

        var deleted = productWriteService.softDeleteProduct(productId);
        assertThat(deleted.active()).isFalse();
        assertThat(deleted.updatedAt()).isAfter(updatedAt);

        assertThatThrownBy(() -> productReadService.get(productId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Product not found");

        PagedResponseDto<ProductSummaryDto> listResponse = productReadService.list(0, 20, null);
        assertThat(listResponse.items()).isEmpty();

        PagedResponseDto<ProductSummaryDto> searchResponse = productReadService.list(0, 20, "Phone");
        assertThat(searchResponse.items()).isEmpty();

        verify(productRepository).findByIdTenantIdAndActiveTrue(eq(tenantId), any(Pageable.class));
        verify(productRepository).searchActiveByTenant(eq(tenantId), eq("Phone"), any(Pageable.class));
    }
}
