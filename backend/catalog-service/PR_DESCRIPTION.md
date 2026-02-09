## How to verify

1. **OWNER can write via gateway**
```bash
curl -i -X POST "http://localhost:8080/api/catalog/products" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "X-ORG-ID: org-a" \
  -H "Content-Type: application/json" \
  -d '{"name":"Phone","description":"Demo","priceCents":129900,"currency":"USD"}'
```

```bash
curl -i -X PUT "http://localhost:8080/api/catalog/products/$PRODUCT_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "X-ORG-ID: org-a" \
  -H "Content-Type: application/json" \
  -d '{"name":"Phone v2","description":"Updated","priceCents":139900,"currency":"USD"}'
```

```bash
curl -i -X DELETE "http://localhost:8080/api/catalog/products/$PRODUCT_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "X-ORG-ID: org-a"
```

2. **CLIENT is forbidden on write endpoints**
```bash
curl -i -X POST "http://localhost:8080/api/catalog/products" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H "X-ORG-ID: org-a" \
  -H "Content-Type: application/json" \
  -d '{"name":"Phone","description":"Demo","priceCents":129900,"currency":"USD"}'
```

Expected: `403 Forbidden`.

3. **Cross-tenant isolation**
```bash
curl -i "http://localhost:8080/api/catalog/products/$PRODUCT_ID" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "X-ORG-ID: org-b"
```

Expected: not found/forbidden for tenant B when product belongs to tenant A.
