---
name: flashsale-frontend-skill
description: Plan, implement, and review FlashSale Angular stories using feature-first structure, strict TypeScript, data-access API services, mandatory interceptors (auth + org + correlation + error), clean RxJS patterns, and minimum tests.
---

# FlashSale Frontend Skill

## Trigger Rules
- Trigger when the user says Use FRONTEND SKILL to PLAN.
- Trigger when the user says Use FRONTEND SKILL to BUILD.
- Trigger when the user says Use FRONTEND SKILL to REVIEW.
- Trigger ONLY for Angular frontend tasks.
- Do NOT trigger for Spring Boot/backend tasks.

## Non-Negotiable Rules
1. Feature-first structure: `src/app/core`, `src/app/shared`, `src/app/features/<feature>/(pages|components|data-access)`.
2. Smart vs dumb components: pages = orchestration + state; components = Inputs/Outputs only; no HTTP.
3. HTTP rule: NO `HttpClient` in components/pages; ALL HTTP calls only in `features/*/data-access/*api.ts`.
4. All API calls must go through the gateway path `/api/**` (no direct microservice URLs).
5. Mandatory interceptors:
   - `authInterceptor` adds `Authorization: Bearer <token>`.
   - `orgInterceptor` adds `X-ORG-ID` ONLY for tenant-scoped calls and MUST NOT add it to `/public/**` calls.
   - correlation interceptor adds `X-CORRELATION-ID`.
   - error interceptor maps server errors `{code,message}` to user-friendly UI and NEVER displays stack traces.
6. Tenant selection rule: `activeOrgId` stored in `sessionStorage` and used by `orgInterceptor`.
7. RxJS rules: Prefer async pipe; if manual subscribe, use `takeUntilDestroyed` (or equivalent) to avoid leaks.
8. Security UI rule: Never bypass Angular sanitization and never use `bypassSecurityTrust*` unless explicitly required and reviewed.

## Mode: PLAN
### Expected Inputs
- Story goal and acceptance criteria.
- Target route(s), role expectations, and tenant/public endpoint scope.
- API contracts or endpoint assumptions.

### Output format (required markdown template)
```markdown
## PLAN
### UI Flow + Routes
### Data Access (APIs)
### State + Models
### File List
### Steps
### Tests
### Done
```

### Checklist steps
- Define routes + guards and role-based UI visibility.
- Enumerate endpoints and classify tenant-scoped vs `/public/**`.
- Specify page state handling: loading, empty, error.
- List files in `core/shared/features` placement.
- Define minimum tests (interceptors required, service/page as needed).

### Copy/paste prompt template
```text
Use FRONTEND SKILL to PLAN
Story: <story name>
Goal: <what user should be able to do>
Routes: <new/updated routes>
Roles: <role behavior>
APIs: <endpoints and methods>
Constraints: <tenant/public, UI states, validation>
```

## Mode: BUILD
### Expected Inputs
- Approved plan or clear implementation scope.
- Existing Angular module/standalone structure.
- DTO and endpoint details.

### Output format (required markdown template)
```markdown
BUILD
Files Changed
Implementation Notes
Interceptors + Headers
UI States (loading/empty/error)
Tests Added/Updated
Verification Results
Follow-ups
```

### Checklist steps
- Implement feature-first files with strict typing.
- Keep HTTP access only in `data-access/*api.ts`.
- Ensure interceptors are wired and header rules enforced.
- Implement loading/empty/error UI states.
- Add or update minimum tests, then verify build/test status.

### Copy/paste prompt template
```text
Use FRONTEND SKILL to BUILD
Story: <story name>
Implement: <specific scope>
Files to touch: <if constrained>
APIs/DTOs: <details>
Done criteria: <what must pass>
```

## Mode: REVIEW
### Expected Inputs
- PR diff or changed files list.
- Story acceptance criteria and architectural constraints.
- Test results (if available).

### Output format (required markdown template)
```markdown
REVIEW FINDINGS

[Severity] <finding title> - <file>
Impact: <impact>
Required Fix: <action>

COVERAGE CHECK
Architecture
Tenant + Auth Headers
UI States
RxJS Correctness
Security
Tests
REVIEW VERDICT

<approve / request changes>
```

### Checklist steps
- Validate architecture and file placement.
- Verify tenant/auth/correlation/error interceptor behavior.
- Verify UI states and RxJS subscription safety.
- Verify security posture and test coverage.
- Produce severity-ranked findings and final verdict.

### Copy/paste prompt template
```text
Use FRONTEND SKILL to REVIEW
Scope: <PR/files>
Story criteria: <acceptance criteria>
Review focus: <architecture/tenant/rxjs/security/tests>
```

## Planning Good Practice
Before coding, decisions must be explicit for:
- Routes + guards (if needed).
- Which endpoints are called.
- Role-based UI behavior (hide/disable OWNER actions).
- UI states (loading/empty/error).
- Interceptors behavior (especially `/public/**` excludes `X-ORG-ID`).
- Minimal tests to add.
- File placement rules (`core/shared/features`).

## Review Good Practice
Stop-ship checklist:
- No `HttpClient` usage in components/pages.
- All API calls centralized in data-access.
- `authInterceptor` + `orgInterceptor` installed and correct.
- `/public/**` does not send `X-ORG-ID`.
- async pipe preferred; no subscription leaks.
- error handling does not leak stack traces.
- basic tests exist for interceptors (and key page optional).

Common anti-patterns to flag:
- HTTP in components.
- business logic in templates.
- manual subscriptions without cleanup.
- `bypassSecurityTrust` usage.
- sending `X-ORG-ID` to public endpoints.
- direct service URLs bypassing gateway.

## Fix Problems / Debug Playbook
| Symptom | Likely Cause | Fix Steps |
|---|---|---|
| 401 Unauthorized | Missing token, expired token, wrong interceptor order | Check token source/expiry, verify `authInterceptor` registration and order, retest protected API call. |
| 403 Forbidden | Missing role, missing org, wrong `X-ORG-ID`, org not selected | Validate role claims, ensure org selected, confirm correct `X-ORG-ID`, verify backend authorization expectations. |
| 400 Org required | `activeOrgId` missing, `orgInterceptor` not running | Confirm `sessionStorage.activeOrgId`, check interceptor wiring and exclusion logic, retest tenant endpoint. |
| CORS/proxy issues | Dev proxy misconfigured to gateway | Verify `proxy.conf` target/path rewrite, ensure requests go through `/api/...`, restart dev server. |
| API returns but UI empty | Mapping bug, wrong DTO fields, not handling paging | Inspect response in Network tab, align DTO typings/mappers, implement paging binding and empty state. |
| RxJS leaks | Subscriptions not cleaned | Replace manual subscriptions with async pipe where possible, otherwise use `takeUntilDestroyed`/equivalent and verify teardown. |

Fix Workflow:
1. Reproduce in browser DevTools Network tab.
2. Confirm headers (`Authorization` + `X-ORG-ID` rules).
3. Confirm endpoint path goes through gateway (`/api/...`).
4. Confirm response shape vs DTO typing.
5. Add/adjust a unit test (interceptor/service) when sensible.
6. Implement fix.
7. Rerun tests.

## Change Safely / Refactor Rules
- Backwards compatible UI changes first.
- Do not break routes/links without redirect.
- Evolve DTOs safely (add optional fields first).
- Split oversized stories (UI scaffold first, then wiring, then polish/tests).

## Definition of Done (Frontend Story)
- Works end-to-end through gateway.
- Correct headers via interceptors (and `/public/**` exclusion).
- No `HttpClient` in components/pages.
- Loading/empty/error states implemented.
- Tests: at least interceptor tests (+ service test recommended).
