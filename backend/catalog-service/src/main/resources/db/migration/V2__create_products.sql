-- Additive migration: keep V1 as the source of table creation.

CREATE INDEX IF NOT EXISTS idx_products_tenant_id_active
    ON products (tenant_id, active);

-- Optional: case-insensitive search/sort by name per tenant
CREATE INDEX IF NOT EXISTS idx_products_tenant_id_lower_name
    ON products (tenant_id, lower(name));
