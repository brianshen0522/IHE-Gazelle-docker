# Gazelle realm migration

<!-- TOC -->
* [Gazelle realm migration](#gazelle-realm-migration)
  * [How it works](#how-it-works)
  * [Repeatable migrations](#repeatable-migrations)
  * [Manual migration execution](#manual-migration-execution)
  * [JSON fragments](#json-fragments)
  * [Add a new migration](#add-a-new-migration)
  * [To be improved](#to-be-improved)
<!-- TOC -->

Keycloak doesn't support natively realm migrations. We developed our own solution based on `keycloak-config-cli` to perform these migrations.

Link to the **keycloak-config-cli** repository: [https://github.com/adorsys/keycloak-config-cli](https://github.com/adorsys/keycloak-config-cli).

> :warning: **WARNING** If you update Keycloak in a new version with updated model, you must upgrade the keycloak-config-cli version 
> to the same version of the Keycloak server (see the [Upgrade section](../README.md) in GUM documentation).

## How it works

The logic of the migration is based on the `flywaydb` migration tool. At each deployment of GUM, a script will be executed in order to check if there is migration to be executed.

All of this logic is implemented in Bash in the script named `keycloak-migration.sh`.

This script will browse all the directories names in the `realm-migration` directory. Then it will compare the last migrated migration to these directory names.

If one of these directories is greater than the last migrated migration, it will execute the migration.

To execute migration, the script will use the `keycloak-config-cli` jar. Thanks to provided variables in the `.env` file, the script will be able to connect to the Keycloak server and execute the migration.

In each directory, there is JSON files which represent a model of a Keycloak resources. The `keycloak-config-cli` will use these JSON files to create or update these resources in the Keycloak.

> :warning: **WARNING** The migrations are idempotent. It means that you can execute the migration multiple times without any side effects. 
> But the values in the JSON files can erase the custom configurations performed in the GUI.

The persistence of the last migrated version is done in a file named `/opt/app-version/gazelle-user-management-keycloak`.

The file juste contains the last migrated version (ex : `2.0.0`). If this file doesn't exist, `keycloak-migration.sh` will create it and will execute all the migrations.

> :warning: **WARNING** For the moment, it's not possible to delete a resource. See the [To be improved](#to-be-improved) section.

The application will not be considered as **HEALTHY** as long as the migrations are not completely executed. 

There is a custom healthcheck that check if the last migrated version is equals to the current version of GUM.

## Repeatable migrations

There is a specific directory named `repeatable` in the `realm-migration` directory. 
This directory is used to store JSON files too. But these JSON files will be executed at each deployment of GUM.

For the moment, there is two repeatable migrations :

- gazelle-clients-admin.json : Update the password or the email of the specific admin used for M2M and CAS registrations .
- realm-configuration.json : Update some realm configurations like SMTP for email sending.

## Manual migration execution

There is a script that allows an administrator to execute the migration manually. This can be useful for testing or to resolve potential issues.

> :warning: **WARNING** Execute manual migration can be risky because it will erase all the custom configurations performed in the GUI.

For this, you can use the `execute-migration-manually.sh` which need a parameter like below :

```bash
./execute-migration-manually.sh <folderName>
```

For example, these commands that can be executed :
```bash
```bash
./execute-migration-manually.sh ./1.0.0
./execute-migration-manually.sh ./repeatable
./execute-migration-manually.sh ~/workspace/ans-keycloak-resources/realm-migration
./execute-migration-manually.sh ~/workspace/italian-keycloak-resources/realm-migration
```

## JSON fragments

Each migration is a JSON file that represents a model of a Keycloak resource.
There is a specific header in each JSON file that allows to specify realm destination of the migration and the type of resource.

Simplified example of fragment :
```json
{
  "enabled": true,
  "realm": "$(env:REALM:-gazelle)",
  "clientScopes": [
    ...
  ]
}
```

It's possible to put some variable in these fragments. `keycloak-config-cli` will replace variables before executing the migration.

## Add a new migration

Is not exist, create a new directory in `/realm-migration` with version number of the migration (example : `3.1.0`).

Create a __JSON__ file in this directory for each resource you want to add or update. Name this file by the resource that it represents.

Export the configuration in JSON file from the Keycloak administration console and put it in the directory.

Test the migration by executing manual migration or on new realm by using `REALM` environment variable.

> [NOTE]: It's possible to set a specific order of execution the migration by adding a number at the beginning of the file name.
> (example : 1-realm.json, 2-client.json, 3-user.json).

## To be improved

- Add possibility to execute .sh scripts in migrations in order to perform **deletions** with `kcadmin.sh` command line client.

- Add possibility to retrieve external migration files from another project in order to be able to import configurations for a specific environment (ex : authentication delegation).
