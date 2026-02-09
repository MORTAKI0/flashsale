package com.flashsale.catalog.product.dto;

import java.util.UUID;

public record ProductDetailDto(
    UUID productId,
    String name,
    String description,
    long priceCents,
    String currency,
    boolean active
) {
}
