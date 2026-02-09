package com.flashsale.catalog.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @NotBlank @Size(min = 2, max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull @Min(0) Long priceCents,
        @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]{3}$") String currency

) {
}
