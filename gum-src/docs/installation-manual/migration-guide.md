# Migration guide

This guide gives information for GUM migration from a version to another.

## GUM V4.6.0 -> GUM V5.0.0

- Upgrade to **Java 25** and **Quarkus 3.33**
- Upgrade to **Keycloak version 26.6.3**
- **Migration of organization** from TM database to GUM database. This migration will be done during the first startup of GUM V5.0.0.
- **Mosquitto** integration for event driven architecture.

> [!warning]:warning: Warning Organization migration can be blocked by duplicate organization keywords in TM database.
>
> Before upgrading to GUM05 please ensure there is no duplicate organization keyword in TM database. You can check it with the following query :
> `SELECT keyword, COUNT(*) FROM usr_institution GROUP BY keyword HAVING COUNT(*) > 1;`
> If there are duplicates, you can either delete the duplicates or change their keyword to make them unique (depending on user last login date for example).
> Do not hesitate to contact Gazelle functional team if you need help with this migration.

Some environment variables have changed :

```markdown
- (for quarkus module) [NEW] MP_MESSAGING_OUTGOING_ORGANIZATION-MANAGEMENT-OUT_SSL ex : `MP_MESSAGING_OUTGOING_ORGANIZATION-MANAGEMENT-OUT_SSL=true`
- (for keycloak module) [NEW] MQTT_SSL_ENABLED ex : `MQTT_SSL_ENABLED=true`
- [NEW] MQTT_HOST ex : `MQTT_HOST=localhost`
- [NEW] MQTT_PORT ex : `MQTT_PORT=1883`
- [NEW] MQTT_USERNAME ex : `MQTT_USERNAME=gazelle`
- [NEW] MQTT_PASSWORD ex : `MQTT_PASSWORD=changeit`
```

## GUM V4.5.0 -> GUM V4.6.0

- Integration of Gazelle framework in version 4.0.0 with security improvements and fixes.
- Add service registry registration at startup (with websockets)
- Fix user role not created on first startup
- Swagger is displayed by default (`QUARKUS_SWAGGER_UI_ENABLED=true`)

Some environment variables have changed :

```markdown
- `JWT_VERIFY_PUBLIC_KEY_LOCATION` => `MP_JWT_VERIFY_PUBLICKEY_LOCATION`
- `JWT_VERIFY_ISSUER"` => `MP_JWT_VERIFY_ISSUER` old syntax is not supported anymore
- [NEW] `GZL_JWT_VERIFY_AUDIENCE` ex : `GZL_JWT_VERIFY_AUDIENCE=${SCHEME}://${FQDN}`
- [NEW] `GZL_USER_REGISTRATION_ENABLED` ex : `GZL_USER_REGISTRATION_ENABLED=true`
- [NEW] `GZL_SERVICE_REGISTRY_URL` ex : `GZL_SERVICE_REGISTRY_URL=http://localhost:8088/service-registry`
```

## GUM V4.4.0 -> GUM V4.5.0

New variable to define privacy policy URL

```dotenv
GZL_PRIVACY_POLICY_URL="https://www.ihe-europe.net/privacy-policy"
```

## GUM V4.3.0 -> GUM V4.4.0

Some environment variables have changed :

```markdown
- `QUARKUS_SWAGGER_UI_ENABLE` => `QUARKUS_SWAGGER_UI_ENABLED`
- `DEBUG_PORT="*:18787"` => `DEBUG_PORT=18787` old syntax is not supported anymore
```

## GUM V4.2.0 -> GUM V4.3.0

- Added Keycloak migrations for Proxy 03 external application registration.
- Added two REST endpoints to get limited users information
- Updated TM path in env variables

## GUM V4.0.0 -> GUM V4.2.0

This upgrade include an upgrade of Keycloak in version `26.2.5`. This version contains some security fixes and improvements.

### Realm migration

The Keycloak realm `gazelle` will be migrated to the new version (`4.2.0/gzl-user-session-count-limit.json`).

This migration will set a limit of concurrent sessions to 3 for the user. This means that if a user is already logged in on 3 different devices, the next login will invalidate the oldest session.

> [WARNING] JWT delivered by a removed session will not be invalidated, they will still be valid until their expiration time.

### Environment variables

Somme environment variables have been removed:

```dotenv
KC_HOSTNAME_URL
KC_HOSTNAME_STRICT_HTTPS
KC_HOSTNAME_PORT
```

Only the following environment variable is required:

```dotenv
KC_HOSTNAME=http://localhost:28080/auth
```

Keycloak admin environment variables have been renamed:

```markdown
`KEYCLOAK_ADMIN` => `KC_BOOTSTRAP_ADMIN_USERNAME`
`KEYCLOAK_ADMIN_PASSWORD` => `KC_BOOTSTRAP_ADMIN_PASSWORD`
```

## GUM V3.0.0 -> GUM V4.0.0

This migration is about user preferences migration from gazelle-tm to GUM. There is also roles migrations to groups.

### Database migration

During the first deployment of GUM V4.0.0, the migration of `user preferences` will be done from TM database to GUM database.

New tables will be created in `gum` in order to persist user preferences.

There are also migrations of roles from the table gum.user_roles to gum.user_groups. Because in GUM V4.0.0 everything is group.

For example, the old role `admin_role` will become the group `role:gazelle_admin`.

### Environment variables

The following environment variables can be removed

```dotenv
KC_PROXY
KC_HOSTNAME_URL
```

The following variable has been added

```dotenv
KC_HTTP_ENABLED
```

## GUM V2.0.0 -> GUM V3.0.0

This update is about delegation of authentication new feature.

### Database migration

>:exclamation: **Important:**
> Postgres database now requires the variable _**max_prepared_transactions**_ to be set. You can find the configuration file
> here (for non dockerized databases) _/var/lib/postgresql/14/data/postgresql.conf_. You can set it to 150. A restart of the
> database is required for the update to take effect. You can then check if it worked with this command ```SHOW max_prepared_transactions;```
> when connected to a database.



In this new version there are new tables in order to manage delegated resources.

### Environment variables

There are new possible variable to configure delegation part :

```dotenv
GZL_EXTERNAL_PROVIDER_JAR_URLS
GZL_IDP_CONTENT_X (replace X by the number of the idp)
GZL_IDP_LOGO_URL_X (replace X by the number of the idp)
```

Refer to the [configuration documentation](installation-guide.md#4-environment-variables) for more information.

## GUM V1.0.0 -> GUM V2.0.0

This migration is huge because it's about user migration from Gazelle-TM to GUM.

## Database migration

This migration of version of GUM will create a new database name `gum` and will migrate all the users from `gazelle` database to the table gum.users of the new database.

During this migration it can have some user conflicts. In the case of two user have the same username case-insensitive, the migration will fail, and we must update manually one of the user in order to fix the conflict.


## Environment variables

replace the following environment variables:

```dotenv
KC_CLIENTS_ADMIN_USERNAME
KC_CLIENTS_ADMIN_PASSWORD
KC_ADMIN_CLIENT_SECRET
```
by these variables :

```dotenv
GZL_SSO_ADMIN_EMAIL
GZL_SSO_ADMIN_USER
GZL_SSO_ADMIN_PASSWORD
```