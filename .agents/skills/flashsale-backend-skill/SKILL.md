---
name: flashsale-backend-skill
description: Plan, implement, and review FlashSale Spring Boot microservice stories with strict tenant isolation (X-ORG-ID + org_ids claim), RBAC, package-by-feature structure, DTO-only APIs, consistent errors, and required security tests.
---

# FlashSale Backend Skill

Use this skill only for FlashSale Spring Boot backend work.

## Trigger Rules

- Trigger when the user says `Use BACKEND SKILL to PLAN`.
- Trigger when the user says `Use BACKEND SKILL to BUILD`.
- Trigger when the user says `Use BACKEND SKILL to REVIEW`.
- Trigger only for Spring Boot backend tasks (controllers, services, repositories, Flyway, security, backend tests).
- Do not trigger for Angular/frontend tasks.

## Non-Negotiable Rules

1. Enforce tenant isolation on every backend path.
2. Require `X-ORG-ID` on authenticated backend requests.
3. Validate `X-ORG-ID` is present in JWT claim `org_ids`; reject access when invalid.
4. Derive `tenant_id` from `X-ORG-ID`, never from request body.
5. Ensure every DB query and mutation filters by `tenant_id`.
6. Apply default RBAC unless requirements explicitly override it:
   - `CLIENT`: read-only
   - `OWNER`: write (create/update/delete) and read
7. Expose DTOs only at API boundaries; never expose entities directly from controllers.
8. Use consistent error format for validation, auth, forbidden, and not-found responses.

## Mode: PLAN

### Expected Inputs

- Story/ticket goal and acceptance criteria
- Affected domain entities and endpoints (if known)
- Existing constraints (security, schema, performance, timeline)

### Output Format

```markdown
## PLAN
### Decisions
### File List
### Steps
### Tests
### Done
```

### Checklist

1. Restate backend scope and explicitly exclude frontend work.
2. Identify controller/service/repository changes and Flyway impact.
3. Define DTO contracts and endpoint behavior.
4. Define tenant flow: read `X-ORG-ID`, validate against JWT `org_ids`, map to `tenant_id`.
5. Define RBAC map with `CLIENT` read and `OWNER` write defaults.
6. Define error contract behavior and status codes.
7. Plan tests for happy path, tenant isolation, RBAC, and error paths.

### Copy/Paste Prompt Template

```text
Use BACKEND SKILL to PLAN
Story: <story summary>
Acceptance Criteria:
- <criterion 1>
- <criterion 2>
Current Backend Context:
- Controllers: <list>
- Services: <list>
- Repositories: <list>
- Security/JWT details: <details>
- DB/Flyway state: <details>
Constraints:
- <constraint 1>
- <constraint 2>
Return this exact format:
## PLAN
### Decisions
### File List
### Steps
### Tests
### Done
```

## Planning Good Practice

Before writing code, make explicit decisions for:

1. API + DTO shape (request, response, validation, backward compatibility)
2. Security + RBAC (`CLIENT` read, `OWNER` write unless overridden)
3. Tenant isolation (`X-ORG-ID` required, `org_ids` membership, `tenant_id` in every query)
4. Data model + indexes (new fields, constraints, query index support)
5. Error contracts (stable code/message/details format)
6. Tests (unit, integration, security, regression)

Always output plan as:

- `Decisions`
- `File List`
- `Steps`
- `Tests`
- `Done`

## Mode: BUILD

### Expected Inputs

- Approved plan or ticket details
- Target package/module locations
- Contract, schema, and security constraints

### Output Format

```markdown
## BUILD
### Files Changed
### Implementation Notes
### Tenant + RBAC Enforcement
### Contract + Error Handling
### Tests Added/Updated
### Verification Results
### Follow-ups
```

### Checklist

1. Implement only backend scope in package-by-feature structure.
2. Keep controllers thin: DTO mapping + orchestration only.
3. Keep business logic in services and data access in repositories.
4. Require `X-ORG-ID` and validate it against JWT `org_ids`.
5. Ensure all repository queries/mutations include `tenant_id` filter.
6. Enforce RBAC defaults (`CLIENT` read, `OWNER` write) at endpoint/service boundaries.
7. Preserve DTO-only API boundaries.
8. Keep consistent error format in all error responses.
9. Add/update Flyway migrations for schema changes.
10. Add/update tests for happy path, tenant isolation, RBAC, and failure cases.

### Copy/Paste Prompt Template

```text
Use BACKEND SKILL to BUILD
Implement this backend story: <story summary>
Plan (if available): <paste plan or "none">
Code Areas:
- Controllers: <paths>
- Services: <paths>
- Repositories: <paths>
- Security: <paths>
- Flyway: <paths>
Hard Requirements:
- tenant_id must come from X-ORG-ID (never request body)
- Validate X-ORG-ID in JWT org_ids
- Every DB query filters tenant_id
- RBAC default: OWNER write / CLIENT read
- DTO-only APIs
- Consistent error format
- Tests: happy path + tenant isolation + RBAC
Return BUILD output format with concrete file-level changes and test results.
```

## Mode: REVIEW

### Expected Inputs

- PR diff or list of changed backend files
- Story acceptance criteria
- Security and tenancy requirements

### Output Format

```markdown
## REVIEW FINDINGS
1. [Severity] <finding title> - <file:line>
   Impact: <impact>
   Required Fix: <action>

## COVERAGE CHECK
### Tenant Isolation
### RBAC
### DTO + Contract
### Error Handling
### Flyway/Data
### Tests

## REVIEW VERDICT
<approve / request changes>
```

### Checklist

1. Confirm backend-only scope; flag any frontend leakage.
2. Verify `X-ORG-ID` is required and validated against JWT `org_ids`.
3. Verify every query/mutation includes `tenant_id` predicate.
4. Verify RBAC behavior: `CLIENT` read only; `OWNER` write unless explicitly changed.
5. Verify DTO-only API boundaries and contract consistency.
6. Verify consistent error handling and stable error schema.
7. Verify Flyway migration safety and code alignment.
8. Verify tests for happy path + tenant isolation + RBAC + negative auth/error cases.

### Copy/Paste Prompt Template

```text
Use BACKEND SKILL to REVIEW
Review this backend change for FlashSale.
Changed files/diff: <paste>
Acceptance Criteria:
- <criterion 1>
- <criterion 2>
Hard Requirements:
- tenant_id always derived from X-ORG-ID (never request body)
- Validate X-ORG-ID is in JWT org_ids
- Every DB query filters tenant_id
- RBAC default: OWNER write / CLIENT read
- DTO-only APIs
- Consistent error format
- Tests include happy path + tenant isolation + RBAC
Return REVIEW FINDINGS first, ordered by severity, with file references.
```

## Review Good Practice

### Stop-Ship Checklist

1. Tenant isolation is enforced in every read/write query.
2. RBAC is explicit and correct for each endpoint.
3. Controllers expose DTOs only.
4. API behavior matches contract and acceptance criteria.
5. Error responses follow the shared error schema.
6. Tests cover happy path, RBAC denial, cross-tenant denial, and validation/auth failures.

### Common Anti-Patterns To Flag

- Entities returned or accepted directly in controllers.
- Repositories called directly from controllers.
- Missing `tenant_id` predicate in repository methods/custom SQL.
- Missing or weak `@PreAuthorize` / role checks for write endpoints.

## Fix Problems / Debug Playbook

### Symptom -> Likely Cause -> Fix Steps

| Symptom | Likely cause | Fix steps |
|---|---|---|
| 401 Unauthorized | Wrong issuer/audience/JWT config, missing `Authorization` header, invalid/expired token | Verify security config for issuer/audience; reproduce with known-good token; confirm header format `Bearer <token>`; inspect auth logs without printing token values |
| 403 Forbidden | Role mapping mismatch, `X-ORG-ID` not present in JWT `org_ids` | Check role claims to Spring authorities mapping; validate org membership logic; retest with proper role and org |
| 400 Org required | Missing `X-ORG-ID` header or blank value | Add required header validation in filter/interceptor/controller advice; return consistent 400 error contract |
| 404 Not found but exists | Tenant mismatch or `active`/soft-delete predicate filtering row | Query by id + tenant_id directly in DB; verify active/soft-delete flags; update query and test data |
| Flyway errors | Wrong baseline, checksum mismatch, out-of-order migration changes | Check `flyway_schema_history`; avoid editing applied migrations; create new corrective migration; baseline only when appropriate |
| JPA query issues | Missing tenant predicate, poor indexes, incorrect join/filter order | Add explicit tenant filter to method/query; add/adjust composite indexes; validate generated SQL and execution plan |
| Gateway routing issues | Bad `StripPrefix`, wrong route id/path predicate, wrong backend service port | Verify gateway route config and path rewrite; confirm backend app port and health endpoint; test with curl through gateway and direct service |

### Fix Workflow

1. Reproduce with `curl` (minimal request, explicit headers).
2. Check service and gateway logs; never print raw tokens.
3. Confirm JWT claims (`sub`, roles, `org_ids`, issuer, audience).
4. Confirm `X-ORG-ID` is present and expected.
5. Confirm SQL rows for target `tenant_id`.
6. Add or adjust a failing test first where sensible.
7. Implement fix in smallest safe change set.
8. Rerun targeted tests, then broader regression tests.

## Change Safely / Refactor Rules

### Contract-Safe Endpoint Changes

1. Prefer additive changes first (new optional fields, new endpoints).
2. Keep existing response fields and semantics stable.
3. Deprecate before removal; provide migration window.
4. Version endpoints when behavior must change incompatibly.
5. Update contract tests for both old and new behavior during transition.

### Safe DB Schema Evolution

1. Add new columns as nullable or with safe defaults.
2. Deploy code that writes both old/new shape when needed.
3. Backfill data with controlled migration job/script.
4. Enforce non-null/constraints only after backfill is verified.
5. Add indexes aligned with tenant-filtered access paths.

### Split Oversized Stories

1. Split into contract, persistence, and behavior phases.
2. Land tenant/security foundations before feature expansion.
3. Keep each PR testable and reversible.
4. Preserve endpoint compatibility across intermediate steps.

## Definition Of Done (Backend Story)

1. Backend scope delivered with no unrelated frontend changes.
2. `X-ORG-ID` required and validated against JWT `org_ids`.
3. `tenant_id` always derived from `X-ORG-ID`, never request body.
4. Every DB read/write path filters by `tenant_id`.
5. RBAC enforced with default policy: `OWNER` write, `CLIENT` read (unless explicitly overridden).
6. DTO-only APIs; entities are not exposed at controller boundaries.
7. Consistent error format for validation/auth/forbidden/not-found failures.
8. Flyway migrations are safe and aligned with code behavior.
9. Tests cover happy path, tenant isolation, RBAC, and key negative paths.
10. Relevant backend test suite passes.
