# Service Registry - Copilot Instructions

## Project Overview

Service Registry is a Quarkus-based microservice that provides service registration and lookup capabilities for the IHE Gazelle ecosystem. It consists of three modules:

- **service-registry-api**: API definitions for service registration and lookup
- **service-registry-client**: Client library for services to self-register and lookup other services
- **service-registry**: Server application implementing the registry

## Technology Stack

- **Java 21** (JDK 21 required)
- **Quarkus 3.27** (Framework)
- **Eclipse Microprofile 5.0** 
- **Maven** (Build tool)
- **WebSockets** (Service Registration API)
- **REST** (Service Lookup API)
- **AsyncAPI 3.0** (WebSocket API documentation)
- **OpenAPI/Swagger** (REST API documentation)

## Build Commands

### Unit Tests
```bash
./mvnw clean test
```

### Run Single Test
```bash
./mvnw test -Dtest=ServiceRegistrationTest
./mvnw test -Dtest=ServiceRegistrationTest#testMethodName
```

### Mutation Tests (PIT)
```bash
./mvnw pitest:mutationCoverage
```
Reports are in `target/pit-reports/` of each module.

### Integration Tests
```bash
./mvnw verify
```
This builds Docker images and runs integration tests. If you encounter network issues:
```bash
docker network prune
```

### Dev Mode
```bash
./mvnw quarkus:dev
```
Dev UI available at: http://localhost:8097/q/dev/

### Packaging
```bash
./mvnw package
```
Produces `target/quarkus-app/quarkus-run.jar` and a Docker image.

### Javadoc Verification
```bash
./mvnw javadoc:javadoc -Djavadoc.failOnError=true -Djavadoc.failOnWarnings=true
```

## Architecture

### Module Dependencies
- `service-registry` depends on `service-registry-api`
- `service-registry-client` depends on `service-registry-api`
- `service-registry-api` is standalone (DTOs and interfaces only)

### Package Structure (service-registry module)

```
net.ihe.gazelle.serviceregistry
├── business/              # Business logic layer
│   ├── lookup/           # Service lookup implementation
│   ├── registration/     # Service registration logic
│   └── permission/       # Permission management
└── technical/            # Technical/infrastructure layer
    ├── config/          # Configuration classes
    ├── dao/             # Data access (in-memory + file-based repositories)
    ├── dto/             # Data transfer objects
    ├── factory/         # CDI producer factories
    ├── job/             # Background jobs (file registration startup)
    ├── rest/            # REST controllers for Service Lookup API
    ├── security/        # RBAC security providers
    └── websocket/       # WebSocket endpoints for Service Registration API
```

### Key Design Patterns

**Layering**: Clear separation between `business` (domain logic) and `technical` (infrastructure) packages. Business
layer MUST be framework-agnostic; technical layer handles Quarkus/CDI integration.

**Dependency Injection**: Quarkus CDI is used sparingly. Look for `@ApplicationScoped` beans and factory classes in
`technical/factory/` package. We prefere using constructor parameters in service classes for injection with annotated
Factory bean in technical package.

**Repository Pattern**: `ServiceRepository` has two I/O implementations:
- All services are loaded and indexed in `InMemoryServiceRepository`.
- Self-registered services are registered via a WebSocket connection.
- Manually registered services are loaded via from `services.json` (via the component `FileServiceRepository`)

### APIs Exposed

**Service Registration API (WebSocket)**:
- Path: `ws://localhost:8097/service-registry/service-registration/{instanceId}/{replicaId}`
- Implementation: `RegistrationWebSocketImpl`
- Documentation: AsyncAPI at `/service-registry/service-registration-api/asyncapi.yaml`

**Service Lookup API (REST)**:
- Base: `http://localhost:8097/service-registry/services`
- Implementation: `ServiceController`
- Documentation: OpenAPI at `/service-registry/swagger-ui`

### Configuration

Key properties in `application.properties`:

```properties
quarkus.http.port=8097
quarkus.http.root-path=/service-registry
gzl.service.registry.url=http://localhost:${quarkus.http.port}${quarkus.http.root-path}
gzl.service.registry.file.path=/opt/service-registry/services.json
gzl.service.registry.self.registration.timeout.hours=72
```

Environment variable pattern: Java property `gzl.service.registry.url` → `GZL_SERVICE_REGISTRY_URL`

### Manual Service Registration

Services that don't implement the WebSocket API can be manually registered via JSON file.
See `service-registry/src/test/resources/file-registration/services.json` for the schema.

## Conventions

### Test Environment Variables

Tests require `GZL_SERVICE_K8S_ID` environment variable (format: `{serviceName}-{instanceId}-{replicaId}`). This is set automatically in `pom.xml` for Surefire.

### Skipping Tests

Use Maven properties:
- `-DskipTests` - skip all tests
- `-DskipUTs` - skip unit tests only
- `-DskipITs` - skip integration tests only

### AsyncAPI Updates

After modifying `service-registry/src/main/resources/META-INF/service-registration-api/asyncapi.yaml`:

```bash
asyncapi generate fromTemplate service-registry/src/main/resources/META-INF/resources/service-registration-api/asyncapi.yaml \
  -o service-registry/src/main/resources/META-INF/resources/service-registration-api/ \
  @asyncapi/html-template@3.5.4 -p singleFile=true --force-write
```

Requires `asyncapi` CLI installed (see https://www.asyncapi.com/docs/tools/cli/installation).

### Gazelle Framework Integration

This project uses internal Gazelle dependencies:
- `net.ihe.gazelle:framework` - Core framework BOM
- `net.ihe.gazelle:lang` - Language utilities
- Repository: https://nexus.ihe-catalyst.net/repository/maven-public

### Service Metadata

Services using `service-registry-client` must implement Gazelle Service Metadata (see `service-registry-client/README.md` for details).

## License

Apache License 2.0 - Copyright 2025 IHE International
