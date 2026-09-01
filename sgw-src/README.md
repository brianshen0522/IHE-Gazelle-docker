# simulation-gateway

<!-- TOC -->
* [simulation-gateway](#simulation-gateway)
  * [Running the application in dev mode](#running-the-application-in-dev-mode)
  * [Packaging and running the application](#packaging-and-running-the-application)
  * [Creating a native executable](#creating-a-native-executable)
  * [Exposed endpoints and resources](#exposed-endpoints-and-resources)
    * [Simulation sequence Lookup API (REST)](#simulation-sequence-lookup-api-rest)
    * [Health Check](#health-check)
  * [Configuration](#configuration)
  * [Static legacy simulators integration](#static-legacy-simulators-integration)
  * [License](#license)
<!-- TOC -->

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/simulation-gateway-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Exposed endpoints and resources

Once running, Simulation-Gateway exposes the following endpoints:

### Simulation sequence Lookup API (REST)

[http://localhost:8087/simulation-gateway/rest/simulation/v1/sequences](http://localhost:8087/simulation-gateway/rest/simulation/v1/sequences)

API documentation is available at:
[http://localhost:8087/simulation-gateway/swagger-ui/#/](http://localhost:8087/simulation-gateway/swagger-ui/#/)
or
[http://localhost:8087/simulation-gateway/openapi?format=json](http://localhost:8087/simulation-gateway/openapi?format=json)

### Health Check

[http://localhost:8087/simulation-gateway/health](http://localhost:8087/simulation-gateway/health)

## Configuration

Here is the list of Java properties or env variables that can be used to configure the application:

| Java Property                                                | ENV Variable                                                 | Description                                                                                  | Default Value                             |
|--------------------------------------------------------------|--------------------------------------------------------------|----------------------------------------------------------------------------------------------|-------------------------------------------|
| `quarkus.http.port`                                          | `QUARKUS_HTTP_PORT`                                          | The port on which the application will listen.                                               | `8087`                                    |
| `quarkus.swagger-ui.enable`                                  | `QUARKUS_SWAGGER_UI_ENABLE`                                  | Enable Swagger UI.                                                                           | `true`                                    |
| `gzl.service.registry.url`                                   | `GZL_SERVICE_REGISTRY_URL`                                   | The public URL of the service registry.                                                      | `http://localhost:8088/service-registry`  |
| `gzl.svs.url`                                                | `GZL_SVS_URL`                                                | The public URL of SVS Simulator                                                              | `http://localhost:8601/SVSSimulator/rest` |
| `gzl.simulation.gateway.sequences.cache.max.timeout.minutes` | `GZL_SIMULATION_GATEWAY_SEQUENCES_CACHE_MAX_TIMEOUT_MINUTES` | The timeout in minutes of the internal cache for simulation sequence lookup index retention. | `720`                                     |
| `gzl.simulation.gateway.services.cache.timeout.minutes`      | `GZL_SIMULATION_GATEWAY_SERVICES_CACHE_TIMEOUT_MINUTES`      | The timeout in minutes of the internal available simulation services cache                   | `5`                                       |


## Static legacy simulators integration

Ability to list listening simulators that does not implement simulation APIs.

The user need to know they exist, and how they can perform tests with them.

We would like to list them in the sequence list as any other simulator, but their sequences are not runnable.
They can have a rich description, simulator properties and potential external link to data set or documentation.

Technical solution:

We can trick simulation-gateway to think it is fetching data from simulators, whereas it's only getting static sequences file from http-reverse proxy :
1. write sequences json file that contains sequences with attribute runnable=false;
2. Create a route on http-reverse proxy that serve that file on a location such as https://{fqdn}/static-simulator/{simulator-name}/simulation/v1/sequences
3. Register manually those static simulators in service-registry. Define provided interface "Simulation Service API" URL on the previously define path.

Need to add this apache conf to work and restart docker container :
```text
<IfModule mod_headers.c>
  <LocationMatch "^/static-simulator/">
    Header set Content-Type "application/json"
  </LocationMatch>
</IfModule>
```

Let's take the example of pixm simulator :

The static simulators can then be added manually to service registry :
```json
  {
    "name": "EPR PIXm Connector",
    "version": "1.0.0",
    "instanceId": "1112",
    "replicaId": "1112",
    "description": "Mock simulator acting as a Cross Reference Patient Identity Manager",
    "providedInterfaces": [
      {
        "interfaceName": "Simulation Service API",
        "interfaceVersion": "1.0.0",
        "bindings": [
          {
            "@type": "REST",
            "serviceUrl": "https://example.com/static-simulator/pixm"
          }
        ]
      }
    ]
  }
```

Then the static simulation sequences file shall be added to /var/www/html/static-simulator/pixm/simulation/v1/sequences
Here 'sequences' has to be the actual file in json format containing the list of simulation sequences we want to expose as pixm simulator.

## License

```
Copyright 2025 Kereval

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