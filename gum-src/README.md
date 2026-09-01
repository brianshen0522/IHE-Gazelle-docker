# Gazelle User Management

This repository contains User Management modules for Gazelle.

<!-- TOC -->
* [Gazelle User Management](#gazelle-user-management)
  * [Requirements](#requirements)
  * [Build artifacts](#build-artifacts)
  * [Testing](#testing)
  * [Configuration](#configuration)
    * [Environment variables](#environment-variables)
  * [Applications deployment (for dev)](#applications-deployment-for-dev)
    * [Deploy with Maven](#deploy-with-maven)
      * [Check the logs](#check-the-logs)
      * [Stop the services](#stop-the-services)
    * [Deploy with Compose](#deploy-with-compose)
      * [Requirements](#requirements-1)
  * [How to configure the gazelle realm](#how-to-configure-the-gazelle-realm)
  * [Configuration to integrate a CAS Client](#configuration-to-integrate-a-cas-client)
  * [Troubleshooting guide](#troubleshooting-guide)
  * [License](#license)
<!-- TOC -->

## Requirements

- JDK 25+/Maven 3.9.0+
- Docker 27.0.2+
- Docker-compose 2.18.+
- PostgreSQL 14+ with authorized prepared transactions

## Build artifacts

It is possible to generate the artifacts with maven.

This command will directly build the docker image of the project :

- rg.fr-par.scw.cloud/gazelle-snapshot/app/gazelle-keycloak:${project.version}
- rg.fr-par.scw.cloud/gazelle-snapshot/app/gazelle-quarkus:${project.version}

```bash
mvn clean package
```

## Testing

It is possible to run different types of test in this application :

- Unit tests that are run with the following command, the jacoco results can be found in
  module_path/target/site/jacoco/

```bash
mvn clean test
```

- Integration tests that can be run with the following command

```bash
mvn verify
```

- Mutation tests that can be run with the following command, the results can be found in
  module_path/target/pit-reports/

  In keycloak-provider module, the mutation tests are skipped.

```bash
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

Be careful of tests that need environment variables, they will more than likely not work, so you will need to exclude
them from pitest. To do that add the following in the pom.xml of the module, in the configuration section of the pitest
plugin, where the classes to exclude are in :

```xml
<excludedClasses>
    <param>path.of.the.class.to.excludes</param>
    <param>path.of.the.classes.to.exclude.*</param>
</excludedClasses>
<excludedTestClasses>
<param>path.of.the.test.class.to.excludes</param>
<param>path.of.the.test.classes.to.exclude.*</param>
</excludedTestClasses>
```

## Configuration

### Environment variables

There are many environment variables that can be set to configure the applications.

Refer to the [installation guide](docs/installation-manual/installation-guide.md#4-environment-variables) for more
information.

See the [official documentation](https://www.keycloak.org/server/all-config) of Keycloak for more information about the
environment variables.

## Applications deployment (for dev)

### Deploy with Maven

The first possibility is to deploy GUM with maven-docker-plugin. This process is useful for
integrations tests.

```bash
mvn -f gazelle-keycloak/keycloak-provider/pom.xml docker:start
```

Keycloak should be available by default at [http://localhost:28080/](http://localhost:28080/).

The list of applications that will be deployed :

- gazelle-database (https://gitlab.inria.fr/gazelle/private/industrialization/docker/gazelle-database)
- gazelle-user-management-keycloak (The keycloak instance)
- gazelle-user-management-quarkus (The GUM backend micro-service)
- Mailhog for testing mails (https://github.com/mailhog/MailHog)

#### Check the logs

```bash
mvn -f user-management/user-management docker:logs 
```

> :information_source: **Tips:**
> you can add the flag __-Ddocker.follow__ to your command to automatically follow the logs.

#### Stop the services

```bash
mvn -f gazelle-keycloak/keycloak-provider/pom.xml docker:stop
```

### Deploy with Compose

For end-to-end testing you can use the docker-compose to deploy the applications that you need.

#### Requirements

- Data for gazelle-tm
- Environment files (.env)

**1) Start the database**

```bash
docker compose up -d gazelle-database
```

**2) Import data [First deployment only]**

```bash
psql -h localhost -U gazelle -d postgres -c "CREATE DATABASE gazelle;"
psql -h 127.0.0.1 -U gazelle gazelle < ./gazelle-tm-dev-database.sql
```

**3) Configure realm [Optional]**

Copy your realm to `/opt/keycloak/data/import/` (create folders if necessary) if additional realms
are required.

```bash
sudo cp ./realm-gazelle.json /opt/keycloak/data/import/
```

**4) Start GUM**

```bash
docker-compose up -d gazelle-user-management-keycloak gazelle-user-management-quarkus
```

## How to configure the gazelle realm

To understand better how the gazelle realm works and how to configure it,
see [Keycloak administrator manual](docs/admin-manual/keycloak-administrator.md).

## Configuration to integrate a CAS Client

In CAS file .properties of your client application:

```properties
casServerUrlPrefix="http://localhost:28080/realms/gazelle/protocol/cas"
casServerLoginUrl="http://localhost:28080/realms/gazelle/protocol/cas/login"
casLogoutUrl="http://localhost:28080/realms/gazelle/protocol/cas/logout"
service|serverName=${your.app.service|server.name}
```

## Troubleshooting guide

In case of problems you can check this [guide](docs/installation-manual/troubleshooting-guide.md) to see if your problem
is already in here.

## License

```
    Copyright 2022-2026 IHE International
    
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
