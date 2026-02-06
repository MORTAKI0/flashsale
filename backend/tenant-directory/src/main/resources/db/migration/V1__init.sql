CREATE TABLE IF NOT EXISTS tenants (
  id              UUID PRIMARY KEY,
  org_id          VARCHAR(64) NOT NULL UNIQUE,
  name            VARCHAR(255) NOT NULL,
  realm           VARCHAR(255) NOT NULL,
  enabled         BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tenants_org_id ON tenants(org_id);
