package com.flashsale.catalog.shared.error;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
    String code,
    String message,
    String correlationId,
    String path,
    OffsetDateTime timestamp
) {
}
