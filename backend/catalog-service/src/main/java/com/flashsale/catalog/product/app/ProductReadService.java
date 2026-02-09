package com.flashsale.catalog.product.app;

import com.flashsale.catalog.product.domain.ProductEntity;
import com.flashsale.catalog.product.dto.PagedResponseDto;
import com.flashsale.catalog.product.dto.ProductDetailDto;
import com.flashsale.catalog.product.dto.ProductSummaryDto;
import com.flashsale.catalog.product.infra.ProductRepository;
import com.flashsale.catalog.shared.error.NotFoundException;
import com.flashsale.catalog.shared.tenant.TenantContextAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class ProductReadService {

    private final ProductRepository productRepository;
    private final TenantContextAccessor tenantContextAccessor;

    public ProductReadService(
        ProductRepository productRepository,
        TenantContextAccessor tenantContextAccessor
    ) {
        this.productRepository = productRepository;
        this.tenantContextAccessor = tenantContextAccessor;
    }

    public PagedResponseDto<ProductSummaryDto> list(int page, int size, String q) {
        String tenantId = tenantContextAccessor.requiredTenantId();
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductEntity> resultPage;

        if (StringUtils.hasText(q)) {
            resultPage = productRepository.searchActiveByTenant(tenantId, q.trim(), pageable);
        } else {
            resultPage = productRepository.findByTenantIdAndActiveTrue(tenantId, pageable);
        }

        List<ProductSummaryDto> items = resultPage
                .getContent()
                .stream()
                .map(ProductReadService::toSummaryDto)
                .toList();

        return new PagedResponseDto<>(
                items,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    public ProductDetailDto get(UUID productId) {
        String tenantId = tenantContextAccessor.requiredTenantId();
        ProductEntity entity = productRepository
                .findByTenantIdAndProductIdAndActiveTrue(tenantId, productId)
                .orElseThrow(() -> new NotFoundException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found: " + productId
                ));

        return toDetailDto(entity);
    }

    private static ProductSummaryDto toSummaryDto(ProductEntity e) {
        return new ProductSummaryDto(
                e.getProductId(),
                e.getName(),
                e.getPriceCents(),
                e.getCurrency()
        );
    }

    private static ProductDetailDto toDetailDto(ProductEntity e) {
        return new ProductDetailDto(
                e.getProductId(),
                e.getName(),
                e.getDescription(),
                e.getPriceCents(),
                e.getCurrency(),
                e.isActive()
        );
    }
}
