-- Keep tenant-safe composite PK on (tenant_id, product_id).
-- This migration intentionally does not change primary key shape.

ALTER TABLE products
    DROP CONSTRAINT IF EXISTS pk_products;

ALTER TABLE products
    DROP CONSTRAINT IF EXISTS products_pkey;

ALTER TABLE products
    ADD CONSTRAINT pk_products PRIMARY KEY (tenant_id, product_id);

CREATE INDEX IF NOT EXISTS idx_products_tenant_product_id
    ON products (tenant_id, product_id);
