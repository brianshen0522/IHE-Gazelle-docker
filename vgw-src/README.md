# Validation Gateway

Validation Gateway bridges Gazelle services (Maestro, Datahouse, Service Registry) with both the modern gateway contracts and the legacy EVSClient REST surface. The root `pom.xml` orchestrates four active Maven modules (plus a deprecated helper module):

| Module | Role |
| --- | --- |
| `validation-gateway-engine` | Core business logic (profile resolution, caching, DTOs, helpers) reused by both adapter layers. |
| `validation-gateway-jaxrs` | Official gateway interface: canonical REST definitions for **profiles** and **validations**. |
| `validation-gateway-evs` | Retro-compatibility adapter for EVSClient endpoints (`/rest/evs/validations`, `/rest/evs/profiles`, legacy `/rest/evs/GetValidation*`). |
| `validation-gateway-app` | Quarkus assembly that wires engine + adapters + security + integration tests. |


## Purpose and Core Flow

1. **Profile discovery**: resolve available validation services and profile lists from Service Registry.  
3. **Backwards compatibility**: preserve EVSClient contracts for existing portals while enabling the official gateway API.

Both the JAX-RS and EVS modules reuse the same engine services, ensuring identical profile resolution, caching, and ACL behavior.

## REST Interfaces (Gateway)

**Official (JAX-RS module)**
- Profiles and validations are defined here and are the **canonical API** for Gazelle clients.  
- This is the reference for the gateway’s standard REST contract.

**Legacy (EVS module)**
- Mirrors EVSClient behavior and payload shapes for `/rest/evs/*` routes.  
- Adds compatibility endpoints like `/rest/evs/validations`, `/rest/evs/validations/profiles`, etc.

## Configuration

Configuration lives primarily in `validation-gateway-evs/src/main/resources/application.properties` and is overridden in `validation-gateway-app/src/main/resources` for environments.

| Property | Purpose |
| --- | --- |
| `evs.api.user-need-to-be-logged-in` | Whether EVS endpoints require authentication before validation creation. |
| `evs.api.read-access-key-length` | Length of generated read-access keys for private validations. |
| `maestro.base-url`, `maestro.m2m.k8s-id-variable-name` | Maestro endpoint + M2M identity variable name. |
| `datahouse.url`, `datahouse.m2m.k8s-id-variable-name` | Datahouse endpoint + M2M identity variable name. |

## Caching (Profiles)

Profiles are cached in `validation-gateway-engine` using `ValidationProfileCache` (Caffeine). The cache enforces:

1. `profileCacheMaxSize` maximum entries.  
2. `profileCacheTtlSeconds` expiration per entry.  
3. `profileCacheFailureCooldownSeconds` to avoid hammering failing services (cached data is reused until the cooldown expires).

`CachingValidationService` wraps resolved services returned by `ServiceRegistryValidationServiceResolver`, so both the JAX-RS and EVS adapters benefit from the same TTL/failure rules.

## Async Validation Behavior (EVS)

When `Prefer: respond-async` is sent to `POST /rest/evs/validations`:

1. The gateway returns `202 Accepted` immediately.  
2. Maestro is called in a background thread (non-blocking for the client).  
3. `GET /rest/evs/validations/{oid}` responds with `202` + `X-Progress=IN_PROGRESS` while pending.  
4. `GET /rest/evs/validations/{oid}/report` returns `404` until the report is ready.  
5. Async state is stored in-memory only (restart clears pending requests).  

There is no Maestro callback endpoint yet (intentionally to keep EVS API simple).

## EVS Module Details (Retro-Compatibility)

The EVS module is not the official gateway surface; it exists to support legacy EVSClient consumers.

**What it provides**
- `/rest/evs/validations` and `/rest/evs/validations/{oid}` that translate between Maestro reports and EVS payloads.
- `/rest/evs/profiles` that maps engine profiles into EVS-compatible profile DTOs.
- `/rest/evs/GetValidation*` endpoints matching legacy EVSClient semantics used in older portals.

**Security / ACL behavior**
- Uses the security module to resolve identities and generate read-access keys for private reports.  
- ACL enforcement is shared with the engine logic to keep behavior aligned across adapters.

**Important differences vs official gateway**
- Accepts EVS legacy payload formats and media types.  
- Adds older utility endpoints absent from the canonical gateway API.

## Getting Started

### Prerequisites
1. Java 21 (enforced through `maven.compiler.release`).  
2. Maven 3.9+ (or use the wrapper in `validation-gateway-app`).  
3. Docker (required for integration tests with Testcontainers).

### Build
```bash
mvn clean verify
```
Use `-DskipITs=true` to skip integration tests.

### Run the assembled gateway
```bash
cd validation-gateway-app
./mvnw compile quarkus:dev
```

## Testing

- **Unit tests**: run per module (e.g. `mvn -pl validation-gateway-evs test`).  
- **Integration tests**: `validation-gateway-app/src/it` (uses Testcontainers for Keycloak, Maestro, Datahouse, Service Registry).  
- **Security snapshot**: ensure `security-4.0.0-SNAPSHOT` is available in your local Maven cache.

## Troubleshooting

- Missing `net.ihe.gazelle.m2m.client.technical.filter.M2MHttpClientBuilder` typically means your security snapshot is outdated (`mvn -U` to refresh).  
- Service Registry M2M registration errors (e.g. `ClientM2MRegisterJob` NPE) indicate mismatched Keycloak mocks or invalid `gzl.sso.url`.  
- Profile count mismatches usually trace back to `SearchProfileService` and its cache resolution (engine module).
