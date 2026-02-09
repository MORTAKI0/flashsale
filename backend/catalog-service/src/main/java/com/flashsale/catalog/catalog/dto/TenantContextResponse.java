package com.flashsale.catalog.catalog.dto;

import java.util.List;

public record TenantContextResponse(
    String tenantId,
    String userId,
    List<String> roles,
    String correlationId
) {
}
