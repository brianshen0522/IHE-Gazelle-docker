#!/bin/bash

##
# Copyright 2024 IHE International.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##

##
# @author Valentin Lorand
# @date 05/10/2024
##
# This script define performMigration() method that can be used by any Gazelle application for migrations
# To use the gazelle-migration.sh, create your own migration.sh script and import this script
# Then, you can call performMigration() method with three parameters as follow:
#
# PERSISTENT_VERSION_FILE="/opt/app-version/gazelle-user-management-keycloak"
# GZL_MIGRATION_REPEATABLE_MIGRATION_ENABLED="true"
# GZL_MIGRATION_VERSION_FOLDER="/realm-migration/*"
# performMigration "${PERSISTENT_VERSION_FILE}" "${GZL_MIGRATION_REPEATABLE_MIGRATION_ENABLED}" "${GZL_MIGRATION_VERSION_FOLDER}"
#
# Don't forget to overload executeRepeatableMigration() and executeVersionMigration() depending on your need.
# For orchestration configurations, you must mount a volume for the folder in order to persist it on the server.
# Refer to projects using the migration script (gazelle-user-management-keycloak, gazelle-hl7v2-validator-quarkus)
#
# If you need to update this script, refer to the sources of gazelle-user-management application.

GREEN='\033[1;32m'
ORANGE='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m'

# This function need to be overloaded by the script that call performMigration() function
function executeVersionMigration() {
    echo "executeVersionMigration function must be overloaded"
    exit 1
}

# This function need to be overloaded by the script that call performMigration() function
function executeRepeatableMigration() {
    echo "executeRepeatableMigration function must be overloaded"
    exit 1
}

function performMigration() {
  PERSISTENT_VERSION_FILE=$1
  GZL_MIGRATION_REPEATABLE_MIGRATION_ENABLED=$2
  GZL_MIGRATION_VERSION_FOLDER=$3

  # Check if there are any migration file to be executed
  if [ ! -f "${PERSISTENT_VERSION_FILE}" ]; then
      echo -e "${ORANGE}⚠ Versioning file (${PERSISTENT_VERSION_FILE}) doesn't exist. Let's create it and then execute all migrations.${NC}"
      mkdir -p "$(dirname "${PERSISTENT_VERSION_FILE}")"
      echo "0.0.0" > "${PERSISTENT_VERSION_FILE}"
      LAST_VERSION_MIGRATED=$(cat "${PERSISTENT_VERSION_FILE}")
  else
    LAST_VERSION_MIGRATED=$(cat "${PERSISTENT_VERSION_FILE}" 2>/dev/null)
    if [ -z "${LAST_VERSION_MIGRATED}" ]; then
        echo "0.0.0" > "${PERSISTENT_VERSION_FILE}"
        LAST_VERSION_MIGRATED=$(cat "${PERSISTENT_VERSION_FILE}")
    fi
  fi

  # Remove potential postfix (-SNAPSHOT or ticket number)
  APP_VERSION_CLEAN=$(echo "${APP_VERSION}" | grep -oP '(\d+\.\d+\.\d+)')

  # Check that current APP_VERSION equals to persisted version
  if [ "${APP_VERSION_CLEAN}" == "${LAST_VERSION_MIGRATED}" ]; then
      echo -e "${GREEN}✔ Up to date (${APP_VERSION_CLEAN}), no upgrade migration required.${NC}"
  else
      echo -e "Migrations need to be performed from version $LAST_VERSION_MIGRATED to $APP_VERSION_CLEAN."
      # Iterate on all migration files and compare version X.Y.Z with the last version migrated and the current version
      for dir in ${GZL_MIGRATION_VERSION_FOLDER}/* ; do # Do not add quotes here or you will loose 1 hour
          # Extract version from file name
          VERSION=$(echo "$dir" | grep -oP '(\d+\.\d+\.\d+)')

          # Compare version with last version migrated and current version
          if [ "$(echo "${VERSION}" | tr '.' ' ' | awk '{print $1*10000+$2*100+$3}')" -gt "$(echo "${LAST_VERSION_MIGRATED}" | tr '.' ' ' | awk '{print $1*10000+$2*100+$3}')" ] \
            && [ "$(echo "${VERSION}" | tr '.' ' ' | awk '{print $1*10000+$2*100+$3}')" -le "$(echo "${VERSION}" | tr '.' ' ' | awk '{print $1*10000+$2*100+$3}')" ]; then
              echo -e "Migrating from version ${LAST_VERSION_MIGRATED} to ${VERSION}..."

              executeVersionMigration
              if [ $? -ne 0 ]; then
                  echo -e "${RED}✗ The migration for version ${VERSION} failed, exit... ${NC}"
                  exit 1
              fi

              # Persist version
              echo "${VERSION}" > "$PERSISTENT_VERSION_FILE"
              LAST_VERSION_MIGRATED="${VERSION}"
          fi
      done

      echo "$APP_VERSION_CLEAN" > "$PERSISTENT_VERSION_FILE"
      echo -e "${GREEN}✔ Upgrade migrations has been correctly executed.${NC}"
  fi

  if [ "${GZL_MIGRATION_REPEATABLE_MIGRATION_ENABLED:-false}" = "true" ]; then
    executeRepeatableMigration
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ Repeatable migrations failed.${NC}"
        exit 1
    else
      echo -e "${GREEN}✔ Repeatable migrations successfully executed.${NC}"
    fi
  fi
}