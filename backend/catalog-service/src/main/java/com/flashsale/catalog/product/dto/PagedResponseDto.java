package com.flashsale.catalog.product.dto;

import java.util.List;

public record PagedResponseDto<T>(
    List<T> items,
    int page,
    int size,
    long totalItems,
    int totalPages
) {
}
