# Installation manual

<!-- TOC -->
* [Installation manual](#installation-manual)
* [Gazelle Keycloak CAS 26.X.X Installation Manual](#gazelle-keycloak-cas-26xx-installation-manual)
  * [Prerequisites](#prerequisites)
* [Installation](#installation)
  * [1. Keycloak docker image](#1-keycloak-docker-image)
  * [2. Files to provide to keycloak](#2-files-to-provide-to-keycloak)
  * [3. Realm configurations for Gazelle](#3-realm-configurations-for-gazelle)
  * [4. Environment variables](#4-environment-variables)
  * [5. Execute run.sh and start Keycloak](#5-execute-runsh-and-start-keycloak)
<!-- TOC -->

# Gazelle Keycloak CAS 26.X.X Installation Manual

## Prerequisites

Installation needs to be performed on a Debian linux __Stretch__ or newer. This SSO is used by Gazelle tools to authenticate users.
A PostgreSQL instance is required to store the data of the SSO.
Two databases are used by the SSO. One is for keycloak and the other one is the Gazelle TM database used to get the list of users and their group and roles.

# Installation

## 1. Keycloak docker image

Browse the following URL to get the correct docker image of Keycloak : https://quay.io/repository/keycloak/keycloak.

Gazelle integration works with version 26.5.3.

Example of commands to download the image: 

```bash
docker pull quay.io/keycloak/keycloak:26.5.3
```

## 2. Files to provide to keycloak

Download the file named `gazelle-user-management.jar` from the following URL: 
[https://gazelle.ihe.net/nexus/service/local/repositories/releases/content/net/ihe/gazelle/keycloak-provider](https://gazelle.ihe.net/nexus/service/local/repositories/releases/content/net/ihe/gazelle/keycloak-provider)

Download the file named `keycloak-protocol-cas-${KEYCLOAK_VERSION}.jar` from the following URL : 
[https://github.com/jacekkow/keycloak-protocol-cas/releases/download/${KEYCLOAK_VERSION}/keycloak-protocol-cas-${KEYCLOAK_VERSION}.jar](https://github.com/jacekkow/keycloak-protocol-cas/releases/download/${KEYCLOAK_VERSION}/keycloak-protocol-cas-${KEYCLOAK_VERSION}.jar),
with KEYCLOAK_VERSION being the version of Keycloak used in the previous part. This will add the cas protocol to client 
protocol available in Keycloak

The jars must be placed in the folder `/opt/keycloak/providers/`.

Download the file named `createClientAdminUser.sh` from the following URL:
[https://gitlab.inria.fr/gazelle/applications/gazelle-user-management/-/blob/master/keycloak-provider/createClientAdminUser.sh](https://gitlab.inria.fr/gazelle/applications/gazelle-user-management/-/blob/master/keycloak-provider/createClientAdminUser.sh).
This script is used to create a specific client with a user which has only the rights to management clients of Gazelle realm. In this way we avoid using a super admin user to manage clients. 
This user will be used by Gazelle applications to register themselves as a CAS client in Keycloak (for legacy applications). For new applications under JDK17, they will be registered as OIDC clients.

Put this script in the folder `/opt/startup-scripts/`.

## 3. Realm configurations for Gazelle

A JSON configuration file can be provided to correctly configure the Gazelle realm. This file must declare all the configurations of the realm.
You can also, and it's recommended use the [Realm migration system](../../gazelle-keycloak/keycloak-provider/realm-migration/README.md)

The list of configurations to set in the file :

- Global configuration (realm name, user auth flow)
- The list of clients allowed to use the SSO
- The list of roles
- The list of protocol mappers
- The SMTP configuration
- Brute force protection configuration
- Event listeners configuration
- Theme for the login page and emails

An example of this file available in the source code repository of Gazelle User Management 
[https://gitlab.inria.fr/gazelle/applications/gazelle-user-management/-/blob/master/keycloak-provider/realm-gazelle.json](https://gitlab.inria.fr/gazelle/applications/gazelle-user-management/-/blob/master/keycloak-provider/realm-gazelle.json).

## 4. Environment variables

There are variables that must be given to the docker container in order to deploy the application correctly.

| Name                                                         | Description                                                                    | Value Example                                                  |
|--------------------------------------------------------------|--------------------------------------------------------------------------------|----------------------------------------------------------------|
| DB_KC_USER                                                   | Postgres database user for Keycloak                                            | gazelle                                                        |
| DB_KC_PASSWORD                                               | Postgres database password for Keycloak                                        |                                                                |
| DB_KC_HOST                                                   | Postgres database hostname for Keycloak                                        | localhost                                                      |
| DB_KC_PORT                                                   | Postgres database port for Keycloak                                            | 5432                                                           |
| DB_KC_NAME                                                   | Postgres database name for Keycloak                                            | keycloak                                                       |
| DB_GUM_USER                                                  | Postgres database user for TM                                                  | gazelle                                                        |
| DB_GUM_PASSWORD                                              | Postgres database password for GUM                                             |                                                                |
| DB_GUM_HOST                                                  | Postgres database hostname for GUM                                             | localhost                                                      |
| DB_GUM_PORT                                                  | Postgres database port for GUM                                                 | 5432                                                           |
| DB_GUM_NAME                                                  | Postgres database name for GUM                                                 | gum                                                            |
| DB_TM_USER                                                   | Postgres database user for TM                                                  | gazelle                                                        |
| DB_TM_PASSWORD                                               | Postgres database password for TM                                              |                                                                |
| DB_TM_HOST                                                   | Postgres database hostname for TM                                              | localhost                                                      |
| DB_TM_PORT                                                   | Postgres database port for TM                                                  | 5432                                                           |
| DB_TM_NAME                                                   | Postgres database name for TM                                                  | gazelle                                                        |
| DEBUG                                                        | Enable keycloak remote debug mode                                              | false                                                          |
| DEBUG_PORT                                                   | Set keycloak debug port                                                        | *:18787                                                        |
| DEV_MODE                                                     | Start keycloak in dev mode                                                     | false                                                          |
| GZL_EXTERNAL_PROVIDER_JAR_URLS                               | The list of urls of external jars to be retrieve                               | http://localhost/file1.jar,http://localhost/file1.jar          |
| GZL_IDP_LOGO_URL_X                                           | The url of an optional logo to be displayed on login page for the idp number X | http://localhost:8080/logo/idp1_logo.png                       |
| GZL_IDP_CONTENT_X                                            | The url of an optional text to be displayed on login page for the idp number X | If you are an external member, you can log in through this IDP |
| GZL_SSO_DEFAULT_LOCALE                                       | The default language used for user interface, only works when migrating        | en                                                             |
| GZL_TM_URL                                                   | Gazelle-tm url                                                                 | http://localhost:8080/gazelle                                  |
| GZL_SSO_LOGO_URL                                             | The url of the customer logo displayed on the login page                       | http://localhost:8080/gazelle/mylogo.png                       |
| GZL_USER_MANAGEMENT_FRONT_URL                                | The url of the user management front application                               | http://localhost:3000/gazelle/user-management                                   |
| GZL_USER_INACTIVATED_PURGE_AFTER_DAYS                        | The number of days to wait to purge never activated or consented users         | 365 (Default is 31)                                            |
| GZL_USER_CREATION_EMAIL_NOTIFICATION_ENABLED                 | Should an email notification should be sent upon user created by an admin ?    | false (Default is true)                                        |
| GZL_TERMS_OF_SERVICE_URL                                     | Terms of service url                                                           | http://localhost:3000/tos                                      |
| GZL_SSO_ADMIN_EMAIL                                          | The email of the user with manage-client role                                  | Default: indus@kereval.com                                     |
| GZL_SSO_ADMIN_USER                                           | The username of the user with manage-client role                               | gazelle-clients-admin                                          |
| GZL_SSO_ADMIN_PASSWORD                                       | The password of the user with manage-client role                               | password                                                       |
| GZL_SSO_URL                                                  | The URL of the SSO server.                                                     | http://localhost:28080                                         |
| GZL_SSO_REALM                                                | The realm to use                                                               | gazelle                                                        |
| KC_HTTP_PORT                                                 | The HTTP port used by the Keycloak instance                                    | 28080                                                          |
| KC_HTTP_PATH                                                 | The HTTP path used by the Keycloak instance  (used behind reverse proxy)       | /auth                                                          |
| KC_HTTP_RELATIVE_PATH                                        | The relative HTTP path (used behind reverse proxy)                             | /auth                                                          |
| KC_HOSTNAME_URL                                              | The hostname url used by the Keycloak instance (used behind reverse proxy)     | https://production/auth                                        |
| KC_HOSTNAME_PATH                                             | The hostname path used by the Keycloak instance (used behind reverse proxy)    | /auth                                                          |
| KC_SPI_CONNECTIONS_HTTP_CLIENT_DEFAULT_SOCKET_TIMEOUT_MILLIS | Setup Socket inactivity timeout to any outside http request.                   | 10000                                                          |                                                                      |                                                             |
| KC_BOOTSTRAP_ADMIN_USERNAME                                  | The username of the Keycloak admin                                             | admin                                                          |
| KC_BOOTSTRAP_ADMIN_PASSWORD                                  | The password of the Keycloak admin                                             | password                                                       |
| ROOT_TEST_BED_URL                                            | Test bed root url                                                              | http://localhost                                               |
| SMTP_SSL_ENABLED                                             | Enable SSL for SMTP                                                            | false                                                          |
| SMTP_FROM_DOMAIN                                             | Configure from domain (default equals to FQDN variable)                        | localhost                                                      |
| SMTP_MOCK_ENABLED                                            | Enable mock SMTP server                                                        | false                                                          |
| SMTP_USERNAME                                                | SMTP username for authentication                                               |                                                                |
| SMTP_PASSWORD                                                | SMTP password for authentication                                               |                                                                |
| SMTP_PORT                                                    | SMTP server port                                                               | 25                                                             |
| SMTP_HOST                                                    | SMTP server hostname                                                           | localhost                                                      |
| JWT_VERIFY_PUBLIC_KEY_LOCATION                               | The location of the public key used to verify the JWT token                    | ${GZL_SSO_URL}/realms/gazelle/protocol/openid-connect/certs    |
| JWT_VERIFY_ISSUER                                            | The issuer of the JWT token                                                    | ${GZL_SSO_URL}/realms/gazelle                                  |

There are additional variables that can be set to configure the **Keycloak server**.

Refer to the [keycloak server configuration documentation](https://www.keycloak.org/server/all-config) for more information.

## 5. Execute run.sh and start Keycloak

When all the configurations are ready, you can start keycloak with the image of keycloak you retrieved previously.

Don't forget to mount a volume for `/opt/app-version` for persisting realm migration state. Also do not 
forget to either copy the script `keycloak-migration.sh` in the docker in the folder `/opt/startup-scripts/` or mount
a volume for `/opt/startup-scripts`. You can also copy the run.sh script in order to execute it at the start of the container.

Here you can find some useful commands :

```bash
docker cp keycloak-migration.sh keycloak:/opt/startup-scripts/
docker exec -it keycloak chmod +x /opt/startup-scripts/keycloak-migration.sh

docker cp run.sh keycloak:/run.sh
docker exec -it keycloak chmod +x /run.sh
```

If you do not succeed to execute the run.sh script at container startup, you can reproduce the steps in the script manually.

- Create databases of Keycloak and GUM if not exist
- Retrieve potential external JAR for SPI (injection of custom implementations).
- Execute the realm migrations. The migrations require `keycloak-config-cli-X.X.X.jar` to be present and runnable.

Check the logs to follow the progress of the deployment.
When it's deployed, you can access to the admin console with the admin user and the password you set in the environment variables.
You must check the good configuration of the Gazelle realm (required client, client-scope, roles, smtp configuration, brute force protection, etc.).
