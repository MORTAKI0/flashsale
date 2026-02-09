package com.flashsale.catalog.product.app;

import com.flashsale.catalog.product.domain.ProductEntity;
import com.flashsale.catalog.product.domain.ProductEntityId;
import com.flashsale.catalog.product.dto.CreateProductRequest;
import com.flashsale.catalog.product.dto.ProductDetailDto;
import com.flashsale.catalog.product.dto.UpdateProductRequest;
import com.flashsale.catalog.product.infra.ProductRepository;
import com.flashsale.catalog.shared.error.NotFoundException;
import com.flashsale.catalog.shared.tenant.TenantContextAccessor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProductWriteService {

    private final ProductRepository productRepository;
    private final TenantContextAccessor tenantContextAccessor;

    public ProductWriteService(ProductRepository productRepository, TenantContextAccessor tenantContextAccessor) {
        this.productRepository = productRepository;
        this.tenantContextAccessor = tenantContextAccessor;
    }

    public ProductDetailDto createProduct(CreateProductRequest request ) {
        ProductEntity productEntity = new ProductEntity();
        Instant now = Instant.now();
        String tenantId = tenantContextAccessor.requiredTenantId();
        productEntity.setId(new ProductEntityId(tenantId, UUID.randomUUID()));
        productEntity.setCreatedAt(now);
        productEntity.setUpdatedAt(now);
        productEntity.setActive(true);
        productEntity.setName(request.name());
        productEntity.setDescription(request.description());
        productEntity.setPriceCents(request.priceCents());
        productEntity.setCurrency(request.currency());

        productRepository.save(productEntity);
        return new ProductDetailDto(
                productEntity.getProductId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPriceCents(),
                productEntity.getCurrency(),
                productEntity.isActive(),
                productEntity.getCreatedAt(),
                productEntity.getUpdatedAt()
        );

    }

    public ProductDetailDto updateProduct(UUID productId, UpdateProductRequest request) {
        String tenantId = tenantContextAccessor.requiredTenantId();

        ProductEntity productEntity = productRepository
                .findByIdTenantIdAndIdProductIdAndActiveTrue(tenantId, productId)
                .orElseThrow(() -> new NotFoundException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found: " + productId
                ));

        // update allowed fields
        productEntity.setName(request.name());
        productEntity.setDescription(request.description());
        productEntity.setPriceCents(request.priceCents());
        productEntity.setCurrency(request.currency());

        // timestamps
        productEntity.setUpdatedAt(Instant.now());

        ProductEntity saved = productRepository.save(productEntity);

        return new ProductDetailDto(
                saved.getProductId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPriceCents(),
                saved.getCurrency(),
                saved.isActive(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    public ProductDetailDto softDeleteProduct(UUID productId) {
        String tenantId = tenantContextAccessor.requiredTenantId();

        ProductEntity productEntity = productRepository
                .findByIdTenantIdAndIdProductIdAndActiveTrue(tenantId, productId)
                .orElseThrow(() -> new NotFoundException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found: " + productId
                ));

        productEntity.setActive(false);
        productEntity.setUpdatedAt(Instant.now());

        ProductEntity saved = productRepository.save(productEntity);

        return new ProductDetailDto(
                saved.getProductId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPriceCents(),
                saved.getCurrency(),
                saved.isActive(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

}
