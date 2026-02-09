-- Align primary key with JPA identity strategy (@Id on product_id).
-- Keep tenant isolation enforced in all read/write queries.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM products
    GROUP BY product_id
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Cannot switch products PK to product_id: duplicate product_id values exist across tenants';
  END IF;
END $$;

ALTER TABLE products
    DROP CONSTRAINT IF EXISTS pk_products;

ALTER TABLE products
    DROP CONSTRAINT IF EXISTS products_pkey;

ALTER TABLE products
    ADD CONSTRAINT pk_products PRIMARY KEY (product_id);
