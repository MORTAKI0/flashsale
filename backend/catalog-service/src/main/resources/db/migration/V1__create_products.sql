CREATE TABLE products (
                          tenant_id   VARCHAR(64)  NOT NULL,
                          product_id  UUID         NOT NULL,
                          name        VARCHAR(255) NOT NULL,
                          description TEXT         NULL,
                          price_cents BIGINT       NOT NULL,
                          currency    CHAR(3)      NOT NULL,
                          active      BOOLEAN      NOT NULL DEFAULT TRUE,
                          created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                          updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                          CONSTRAINT pk_products PRIMARY KEY (tenant_id, product_id)
);

CREATE INDEX idx_products_tenant_id
    ON products (tenant_id);

CREATE INDEX idx_products_tenant_product_id
    ON products (tenant_id, product_id);
