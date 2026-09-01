#!/bin/bash

source /opt/startup-scripts/gazelle-migration.sh
export PATH=/opt/startup-scripts:$PATH

exec > >(sed -u "s/^/[keycloak-migration.sh] /") 2> >(sed -u "s/^/(stderr) /")

function has_matching_files() {
  local pattern="$1"
  [ -n "$pattern" ] && compgen -G "$pattern" > /dev/null
}

function execute_keycloak_client_cli() {
    # Return early when no input pattern is provided or nothing matches it.
    if ! has_matching_files "$1"; then return 0; fi

  java -jar keycloak-config-cli-26.5.5.jar \
      --keycloak.url=http://localhost:"${KC_HTTP_PORT}""${KC_HTTP_RELATIVE_PATH:-/}" \
      --keycloak.user="${KC_BOOTSTRAP_ADMIN_USERNAME}" \
      --keycloak.password="${KC_BOOTSTRAP_ADMIN_PASSWORD}" \
      --import.files.locations="$1" \
      --import.varsubstitution.enabled=true \
      --debug=false \
      --keycloak.availability-check.enabled=true \
      --keycloak.availability-check.timeout=240s \
      --import.managed.clientscope=no-delete \
      --import.managed.client=no-delete \
      --import.managed.identityprovider=no-delete \
      --import.managed.authenticationflow=no-delete \
      --import.managed.role=no-delete
}

function execute_kc_admin_cli() {
  # Return early when no input pattern is provided or nothing matches it.
  if ! has_matching_files "$1"; then return 0; fi

  /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:"${KC_HTTP_PORT}""${KC_HTTP_RELATIVE_PATH:-/}" \
    --realm master --user "${KC_BOOTSTRAP_ADMIN_USERNAME}" --password "${KC_BOOTSTRAP_ADMIN_PASSWORD}" 2>&1
  while IFS= read -r filename; do
    bash "$filename"
    if [ $? -ne 0 ]; then
      return 1
    fi
  done < <(compgen -G "$1")
}

# Overload executeRepeatableMigration function from gazelle-migration.sh
function executeVersionMigration() {
  # Execute JSON migrations
  execute_keycloak_client_cli "/realm-migration/${VERSION}/*.json"
  if [ $? -ne 0 ]; then
      echo -e "${RED}✗ JSON migrations for version ${VERSION} failed, exit... ${NC}"
      exit 1
  fi

  # Execute BASH migrations
  execute_kc_admin_cli "/realm-migration/${VERSION}/*.sh"
  if [ $? -ne 0 ]; then
      echo -e "${RED}✗ Bash migrations for version ${VERSION} failed, exit... ${NC}"
      exit 1
  fi
}

# Overload executeRepeatableMigration function from gazelle-migration.sh
function executeRepeatableMigration() {
  # Execute repeatable migration
  execute_keycloak_client_cli "/realm-migration/repeatable/*.json"
}

PERSISTENT_VERSION_FILE="/opt/app-version/gazelle-user-management-keycloak"
GZL_MIGRATION_REPEATABLE_MIGRATION_ENABLED="true"
GZL_MIGRATION_VERSION_FOLDER="/realm-migration/*"
performMigration "${PERSISTENT_VERSION_FILE}" "${GZL_MIGRATION_REPEATABLE_MIGRATION_ENABLED}" "${GZL_MIGRATION_VERSION_FOLDER}"

# Signal completion for the container healthcheck (compose gates dependents on this)
touch /tmp/realm-migration-done
