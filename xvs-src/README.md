# XML Validation Service

```
Authors: Achraf Achkari - Anna Adam - AAZIBOU EL GERRAB Nabila
Date: 26-07-2024
Version: 3.0.0
```


## Introduction

This is a simple web service that validates XML documents against Schematron rules. It is based on the [PhSchematron](https://github.com/phax/ph-schematron) library.</br>
This service implements the [Validation Service API](https://gitlab.inria.fr/gazelle/library/validation-service-api) of Gazelle Test Bed.


## Requirements

* Java 21
* Docker (needed by quarkus)
* Optional: Mandrel 21 (for native compilation)


## Usage

### Run the service

This is a quarkus application. You can run it in dev mode with the following command:

```bash
./mvnw compile quarkus:dev
```

You can also build the application with:

```bash
./mvnw package
```

and then run the resulting executable jar with:

```bash
java -jar target/xml-validation-service-${APP_VERSION}-SNAPSHOT-runner.jar
```

Where `${APP_VERSION}` is the version of the application.

If you want to build an _uber-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

> For more information about the different packaging options, see the [Quarkus documentation](https://quarkus.io/guides/maven-tooling#packaging-and-running-your-application).

### Available endpoints

The service exposes the following endpoints:

* `GET /rest/metadata` : returns the service description (extends the [Validation Service API](https://gitlab.inria.fr/gazelle/library/validation-service-api))
* `POST /rest/validation/validate` : validates a XML document against a Schematron ruleset (extends the [Validation Service API](https://gitlab.inria.fr/gazelle/library/validation-service-api))
* `GET /rest/withCache/metadata` : Cache version of `GET /rest/metadata` (see [Caching mechanism](#caching-mechanism))
* `POST rest/withCache/validation/validate` : Cache version of `POST rest/validation/validate` (see [Caching mechanism](#caching-mechanism))

The swagger documentation is available at `/swagger-ui/`.

The metrics are available at `/metrics`.

## Container Startup Migration

This image can run the Schematron Validator migration automatically on container start, with simple versioning:

- A generic migration driver (`gazelle-migration.sh`) records the last executed version in `/opt/app-version/xml-validation-service` and calls a versioned migration.
- For version `1.0.0`, the container runs `src/main/resources/schematron-validator-migration.sh` to extract profiles from the legacy database and build the `index.json` expected by this service.
- The service then starts normally and reads profiles at `/opt/xml-validation-service/index.json`.

Environment variables to control migration:
- `SCHEMATRON_VALIDATOR_SOURCE` (default `/opt/SchematronValidator_prod`)
- `XML_VALIDATION_DEST` (default `/opt/xml-validation-service`)
- `DB_HOST` (default `localhost`), `DB_PORT` (default `5432`)
- `DB_NAME` (default `schematron-validator-prod`)
- `DB_USER` (default `gazelle`), `DB_PASSWORD` (default `gazelle`)

Notes:
- The native image Dockerfile installs the PostgreSQL client since the migration script uses `psql`.
- The `Dockerfile.native` and `Dockerfile.legacy-jar` set an entrypoint that executes migration before launching the app. The `Dockerfile.native-micro` is unchanged and does not run the migration.


## Configuration

The following properties can be configured in the `application.properties` file:
* `quarkus.http.port` : the port on which the service will be exposed
* `quarkus.log.level` : the log level

In addition, the following property can be configured with environment variable:

* `VALIDATION_PROFILES_DEFINITION` : the path to the json file containing the configuration profiles definition

The definition of a configuration profile must respect the following format:  
```
{
    "profileID" : "[profile_id]",
    "profileName" : "[profile_name]",
    "domain" : "[domain]",
    "standards" : ["[standard1]", "[standard2]"],
    "xsdPath" : "[xsd_absolute_path]",
    "schematronPath" : "[schematron_absolute_path or empty]",
    "xsltPath" : "[xslt_absolute_path or empty]",
    "schematronVersion" : "[schematron_version or empty]",
    "xsdVersion" : "[xsd_version or empty]",
    "coveredItems" : ["[covered_item1]", "[covered_item2]"],
    "cacheEnabled" : true/false,
    "available" : true/false
}
```

**Mandatory vs optional profile fields**
- Required: `profileID`, `profileName`, `domain`, `xsdPath`. The XSD file must exist at `rootOfIndexJson/xsdPath`.
- Optional: `schematronPath` (but if set, the file must exist), `xsltPath`, `standards`, `schematronVersion`, `xsdVersion`, `coveredItems`, `available` (defaults to true), `cacheEnabled` (defaults to false).
- Paths are resolved relative to the directory that contains `index.json`; keep that directory structure intact so the service can find the files.

## Caching mechanism

The Ph-Schematron library does not provide a persistent caching mechanism for compiled XSLT files, so it's only 
an in-memory cache. This means that if the service is restarted, the XSLT files will be recompiled. This can be 
a problem if the service is deployed in a container environment, as the container can be restarted at any time.

The service implements a caching mechanism to avoid recompiling the same SCH file, so it saves the XSLT file in 
in the specified provided path in the configuration profiles. The XSLT file is then used to validate the XML document.

As the caching mechanism still in development, it is exposed as a separate endpoint:
* `GET /rest/withCache/metadata` : Cache version of `GET /rest/metadata`
* `POST rest/withCache/validation/validate` : Cache version of `POST rest/validation/validate`

> **Note:** The caching mechanism reduces the compilation time to 80% of the original time for the first compilation.


## Testing

The service is tested with [Quarkus test framework](https://quarkus.io/guides/getting-started-testing).

To run the unit tests, execute the following command:

```bash
./mvnw test
```

To run the integration tests, execute the following command:

```bash
./mvnw verify
```

The integration tests start the service and send requests to it. Thanks to the Quarkus test framework, the service is started in test mode, so it uses an ephemeral port.

## Native Image build

This is an experimental feature, due to the restrictions of GraalVM (reflection & runtime resources).

To build a native image, execute the following command:

```bash
./mvnw package -Pnative
```

You can then execute the resulting executable with:

```bash
./target/xml-validation-service-${APP_VERSION}-SNAPSHOT-runner
```

### Advanced Technical Explorations

As the Ph-Schematron library uses Saxon to compile the XSLT files, there was intense reflection usage. To avoid manual configuration of reflection,
an agent was used to generate all the required configuration in `resources/native-image-config`

The agent could not be used to generate the configuration for the runtime resources, so it was done manually in `resources/native-image-config`.

If you think that the configuration is incomplete, you will need to start the application with
the agent and execute all the possible workflows to generate the missing configuration.<br/>

```bash
java -agentlib:native-image-agent=config-output-dir=src/main/resources/native-image-config -jar target/xml-validation-service-${APP_VERSION}-SNAPSHOT-runner.jar
```

Another option is to complete Integration tests to cover all the possible workflows and then execute the tests with the agent.

```bash
./mvnw -Pnative-image-agent integration-test -Dnative.agent.config=[path_to_native-image-config]
```

In this case The variable `native.image.config` should be set to the path of the `[path_to_native-image-config]` directory.

## License

    ```
    Copyright 2014-2025 IHE International
    
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
