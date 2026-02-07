package com.flashsale.catalog.product.api;


import com.flashsale.catalog.product.app.ProductReadService;
import com.flashsale.catalog.product.dto.PagedResponseDto;
import com.flashsale.catalog.product.dto.ProductDetailDto;
import com.flashsale.catalog.product.dto.ProductSummaryDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/catalog/products")
public class ProductReadController {

    private final ProductReadService productReadService;

    public ProductReadController(ProductReadService productReadService) {
        this.productReadService = productReadService;
    }

    @GetMapping()
    public PagedResponseDto<ProductSummaryDto> list(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(required = false) String q
    ) {
        return productReadService.list(page, size, q);
    }

    @GetMapping("/{productId}")
    public ProductDetailDto get(@PathVariable UUID productId) {
        return productReadService.get(productId);
    }

}
