package com.flashsale.catalog.shared.tenant;

import java.util.List;

public record TenantContext(
    String tenantId,
    String userId,
    List<String> roles,
    String correlationId
) {
}
