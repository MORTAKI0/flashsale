-- V4__product_write_fields.sql
-- Adds write-related fields to products while keeping existing rows valid.

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS name        varchar(255),
    ADD COLUMN IF NOT EXISTS description text,
    ADD COLUMN IF NOT EXISTS currency    char(3),
    ADD COLUMN IF NOT EXISTS active      boolean NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS created_at  timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS updated_at  timestamptz NOT NULL DEFAULT now();

-- Backfill for existing rows (only if columns were newly added and have nulls)
UPDATE products
SET name = COALESCE(name, 'Unnamed product')
WHERE name IS NULL;

UPDATE products
SET currency = COALESCE(currency, 'USD')
WHERE currency IS NULL;

-- Enforce NOT NULL after backfill
ALTER TABLE products
    ALTER COLUMN name SET NOT NULL,
ALTER COLUMN currency SET NOT NULL;

-- Indexes to keep list/search fast (no-op if they already exist)
CREATE INDEX IF NOT EXISTS idx_products_tenant_id_active
    ON products (tenant_id, active);

CREATE INDEX IF NOT EXISTS idx_products_tenant_id_lower_name
    ON products (tenant_id, lower(name));
