package com.flashsale.catalog.product.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.flashsale.catalog.product.infra.ProductRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductEntityPersistenceGuardTest {

    @Mock
    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        when(repository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity entity = invocation.getArgument(0);
            entity.prePersist();
            return entity;
        });
    }

    @Test
    void builderWithoutIdShouldFailFast() {
        ProductEntity entity = ProductEntity.builder()
            .name("X")
            .active(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        assertThrows(IllegalStateException.class, () -> repository.save(entity));
    }

    @Test
    void builderWithIdShouldWork() {
        ProductEntity entity = ProductEntity.builder()
            .id(new ProductEntityId("org-a", UUID.randomUUID()))
            .name("X")
            .active(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        assertDoesNotThrow(() -> repository.save(entity));
    }
}
