package com.flashsale.catalog.product.dto;

import java.util.UUID;

public record ProductSummaryDto(
    UUID productId,
    String name,
    long priceCents,
    String currency
) {
}
