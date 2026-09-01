# Service Registry

Service Registry is a building block that provides a service registration API to register services
and their metadata, and a service lookup API to retrieve the metadata of registered services.

<!-- TOC -->

* [Service Registry](#service-registry)
    * [Build](#build)
        * [Build and unit-testing the application](#build-and-unit-testing-the-application)
        * [Running mutation tests to assess the quality of the unit tests](#running-mutation-tests-to-assess-the-quality-of-the-unit-tests)
        * [Running the application in dev mode](#running-the-application-in-dev-mode)
        * [Packaging and running the application](#packaging-and-running-the-application)
        * [Running integration tests and advanced verifications](#running-integration-tests-and-advanced-verifications)
        * [Update AsyncAPI documentation](#update-asyncapi-documentation)
    * [Exposed endpoints and resources](#exposed-endpoints-and-resources)
        * [Service Registration API (WebSockets)](#service-registration-api-websockets)
        * [Service Lookup API (REST)](#service-lookup-api-rest)
        * [Health Check](#health-check)
    * [Configuration](#configuration)
        * [Manually registering services](#manually-registering-services)
    * [License](#license)

<!-- TOC -->

The project is composed of three modules:

- `service-registry-api`: the API to register and lookup services.
- `service-registry-client`: a client to integrate in a service to register it, but also lookup
  for other services.
  See [Service Registry Client Documentation](service-registry-client/README.md).
- `service-registry`: the server application that provides the service-registry implementation.

This project requires JDK 21 or above and runs on Quarkus 3.15.

## Build

### Build and unit-testing the application

```shell
./mvnw clean test
```

### Running mutation tests to assess the quality of the unit tests

```shell
./mvnw pitest:mutationCoverage
```

Pit reports can be found in `target/pit-reports/` of every module.

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only
> at <http://localhost:8097/q/dev/>.

### Packaging and running the application

The application can be packaged using:

```shell
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory and a Docker image by
default.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the
`target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

### Running integration tests and advanced verifications

To build the docker image, run integration tests on it, you can use the following command:

```shell
./mvnw verify
```

After several failures, quarkus IT may leave network resources open, and may prevent to run any IT
anymore with the error:
`Caused by: java.lang.RuntimeException: Creating container network 'quarkus-integration-test-XXXX' completed unsuccessfully`
In that case, you can purge unused docker networks using:

```shell
docker network prune
```

That will also checks the javadoc but not fails on warnings. To fully verify javadoc, you can use:

```shell
./mvnw javadoc:javadoc -Djavadoc.failOnError=true -Djavadoc.failOnWarnings=true
```

### Update AsyncAPI documentation

First, update the content of the file
`service-registry/src/main/resources/META-INF/service-registration-api/asyncapi.yaml` according to
the modifications of the API.

Then, run the following command:

```shell
asyncapi generate fromTemplate\
 service-registry/src/main/resources/META-INF/resources/service-registration-api/asyncapi.yaml\
 -o service-registry/src/main/resources/META-INF/resources/service-registration-api/\
 @asyncapi/html-template@3.5.4 -p singleFile=true --force-write
```

It requires the `asyncapi` CLI to be installed.
See [https://www.asyncapi.com/docs/tools/cli/installation](https://www.asyncapi.com/docs/tools/cli/installation).

**TODO** setup docker maven goals to integrate asyncapi generation via docker in the build process.

## Exposed endpoints and resources

Once running, Service-Registry exposes the following endpoints:

### Service Registration API (WebSockets)

`ws://localhost:8097/service-registry/service-registration/{instanceId}/{replicaId}`

API documentation is available at:
[http://localhost:8097/service-registry/service-registration-api/index.html](http://localhost:8097/service-registry/service-registration-api/index.html)
or
[http://localhost:8097/service-registry/service-registration-api/asyncapi.yaml](http://localhost:8097/service-registry/service-registration-api/asyncapi.yaml)

### Service Lookup API (REST) (or Service Registration API for Legacy - REST)

[http://localhost:8097/service-registry/services](http://localhost:8097/service-registry/services)

API documentation is available at:
[http://localhost:8097/service-registry/swagger-ui](http://localhost:8097/service-registry/swagger-ui)
or
[http://localhost:8097/service-registry/openapi?format=json](http://localhost:8097/service-registry/openapi?format=json)

### Health Check

[http://localhost:8097/service-registry/health](http://localhost:8097/service-registry/health)

## Configuration

Here is the list of Java properties or env variables that can be used to configure the application:

| Java Property                                          | ENV Variable                                           | Description                                                                                                                                                                                                                                                                   | Default Value                                            |
|--------------------------------------------------------|--------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| NA                                                     | `GZL_SERVICE_K8S_ID`                                   | The Kubernetes ID of the service. This is used to uniquely identify the service accross any Gazelle instances. Should be formated as `{serviceName}-{instanceId}-{replicaId}`, with instanceId being at least 5 hexadecimal chars and replicaId at least 3 hexadecimal chars. | -                                                        |
| `quarkus.http.port`                                    | `QUARKUS_HTTP_PORT`                                    | The port on which the application will listen.                                                                                                                                                                                                                                | `8097`                                                   |
| `gzl.service.registry.url`                             | `GZL_SERVICE_REGISTRY_URL`                             | The public URL of the service registry.                                                                                                                                                                                                                                       | `http://localhost:${QUARKUS_HTTP_PORT}/service-registry` |
| `gzl.service.registry.file.path`                       | `GZL_SERVICE_REGISTRY_FILE_PATH`                       | The path to the file of services to "manually" register (as opposed to self-registered).                                                                                                                                                                                      | `/opt/service-registry/services.json`                    |
| `gzl.service.registry.heartbeat.timeout.minutes`       | `GZL_SERVICE_REGISTRY_HEARTBEAT_TIMEOUT_MINUTES`       | The timeout in minutes after which a self-registered service that does sends a heartbeat will be downgraded from AVAILABLE to UNREACHABLE.                                                                                                                                    | `5`                                                      |
| `gzl.service.registry.self.registration.timeout.hours` | `GZL_SERVICE_REGISTRY_SELF_REGISTRATION_TIMEOUT_HOURS` | The timeout in hours after which an UNREACHABLE self-registered service that does not give signs of life will be remove from the registry.                                                                                                                                    | `72`                                                     |
| `quarkus.swagger-ui.enabled`                           | `QUARKUS_SWAGGER_UI_ENABLED`                           | Enable Swagger UI.                                                                                                                                                                                                                                                            | `true`                                                   |

Here is the list of security properties that can be used to configure the security of the application (verify the token validity) :

| Java Property                      | ENV Variable                       | Description                                 | Example Value                                               |
|------------------------------------|------------------------------------|---------------------------------------------|-------------------------------------------------------------|
| `gzl.sso.url`                      | `GZL_SSO_URL`                      | SSO Server URL                              | ${SCHEME}://${FQDN}/auth                                    |
| `gzl.sso.admin.user`               | `GZL_SSO_ADMIN_USER`               | SSO Admin username                          | gazelle-clients-admin                                       |
| `gzl.sso.admin.password`           | `GZL_SSO_ADMIN_PASSWORD`           | SSO Admin password                          | changeit                                                    |
| `gzl.m2m.client.secret`            | `GZL_M2M_CLIENT_SECRET`            | Secret of registered client in SSO          | secret                                                      |
| `gzl.jwt.verify.audience`          | `GZL_JWT_VERIFY_AUDIENCE`          | Audience expected for tokens                | ${SCHEME}://${FQDN}                                         |
| `mp.jwt.verify.publickey.location` | `MP_JWT_VERIFY_PUBLICKEY_LOCATION` | Location of public key for token validation | ${GZL_SSO_URL}/realms/gazelle/protocol/openid-connect/certs |
| `mp.jwt.verify.issuer`             | `MP_JWT_VERIFY_ISSUER`             | Issuer expected for tokens                  | ${GZL_SSO_URL}/realms/gazelle                               |

For more information, refer to [Framework -> Security Parent -> oidc-websocket-server-authenticator documentation](https://gitlab.inria.fr/gazelle/public/framework/framework/-/blob/master/security-parent/oidc-server-authenticators/oidc-websocket-server-authenticator/README.md)

### Manually registering services

Services that does not implement the **Service Registration API** can be manually registered by adding them into the
`services.json` file.
Please find an example
here: [service-registry/src/test/resources/file-registration/services.json](service-registry/src/test/resources/file-registration/services.json).

## License

```
Copyright 2025 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
