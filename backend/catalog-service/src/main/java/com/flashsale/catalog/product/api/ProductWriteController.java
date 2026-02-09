package com.flashsale.catalog.product.api;

import com.flashsale.catalog.product.app.ProductWriteService;
import com.flashsale.catalog.product.dto.CreateProductRequest;
import com.flashsale.catalog.product.dto.ProductDetailDto;
import com.flashsale.catalog.product.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/catalog/products")
public class ProductWriteController {

    private final ProductWriteService productWriteService;

    public ProductWriteController(ProductWriteService productWriteService) {
        this.productWriteService = productWriteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDetailDto create(@Valid @RequestBody CreateProductRequest request) {
        return productWriteService.createProduct(request);
    }

    @PutMapping("/{productId}")
    public ProductDetailDto update(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productWriteService.updateProduct(productId, request);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable UUID productId) {
        productWriteService.softDeleteProduct(productId);
    }
}
