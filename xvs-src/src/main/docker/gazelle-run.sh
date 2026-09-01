#!/bin/bash
set -euo pipefail

# Try to source generic migration driver if present; otherwise define a fallback
if [ -f /opt/startup-scripts/gazelle-migration.sh ]; then
  # shellcheck disable=SC1091
  source /opt/startup-scripts/gazelle-migration.sh
else
  detect_app_version() {
    if [ -n "${APP_VERSION:-}" ]; then
      ver="$APP_VERSION"
    else
      ver=""
      if [ -f /deployments/quarkus-run.jar ] && command -v unzip >/dev/null 2>&1; then
        if unzip -p /deployments/quarkus-run.jar META-INF/MANIFEST.MF 2>/dev/null | grep -i '^Implementation-Version:' >/dev/null; then
          ver="$(unzip -p /deployments/quarkus-run.jar META-INF/MANIFEST.MF 2>/dev/null | awk -F': ' '/^Implementation-Version:/ {print $2; exit}')"
        fi
      fi
      if [ -z "$ver" ]; then
        ver="1.0.0"
      fi
    fi
    # Normalize snapshot suffix
    echo "${ver%-SNAPSHOT}"
  }

  performMigration() {
    local version_file="${1:-/opt/app-version/xml-validation-service}"
    local repeatable="${2:-false}"
    local version_folder="${3:-/opt/startup-scripts/migration-scripts}"
    mkdir -p "$(dirname "$version_file")" "$version_folder"
    local current_version
    current_version=$(detect_app_version)
    local last_version=""
    if [ -f "$version_file" ]; then last_version="$(cat "$version_file" || true)"; fi
    if [ "$repeatable" != "true" ] && [ "$last_version" = "$current_version" ]; then
      echo "[migration] Already at version $current_version; skipping"; return 0; fi
    export VERSION="$current_version"
    export MIGRATION_PERFORMED=false
    local ran=1
    if type executeVersionMigration >/dev/null 2>&1; then
      if executeVersionMigration; then
        ran=0
      else
        echo "[migration] Versioned migration failed for $VERSION" >&2
        ran=1
      fi
    else
      echo "[migration] executeVersionMigration not defined; nothing to run"
      ran=1
    fi
    if [ "$ran" -eq 0 ]; then
      if [ "${MIGRATION_PERFORMED}" != true ]; then
        echo "[migration] Migration flagged as skipped; not recording version"
        return 0
      fi
      if [ -n "${XML_VALIDATION_INDEX_PATH:-}" ]; then
        if [ -s "$XML_VALIDATION_INDEX_PATH" ]; then
          if echo "$current_version" > "$version_file"; then
            echo "[migration] Recorded version $current_version in $version_file"
          else
            echo "[migration] Warning: unable to write version file at $version_file (permission denied?); continuing"
          fi
        else
          echo "[migration] Skipping version record: index missing or empty at ${XML_VALIDATION_INDEX_PATH}"
          return 0
        fi
      else
        if echo "$current_version" > "$version_file"; then
          echo "[migration] Recorded version $current_version in $version_file"
        else
          echo "[migration] Warning: unable to write version file at $version_file (permission denied?); continuing"
        fi
      fi
    else
      return 1
    fi
  }
fi

PERSISTENT_VERSION_FILE="/opt/app-version/xml-validation-service"
GZL_MIGRATION_REPEATABLE_MIGRATION_ENABLED="false"
GZL_MIGRATION_VERSION_FOLDER="/opt/startup-scripts/migration-scripts"
APP_USER="${APP_USER:-185}"
RUN_AS_ROOT="${RUN_AS_ROOT:-true}"
GZL_MIGRATION_ENABLED="${GZL_MIGRATION_ENABLED:-true}"
XML_VALIDATION_TABPROFILES_PATH="${XML_VALIDATION_TABPROFILES_PATH:-/opt/xml-validation-service-resources/TABProfilesConfiguration.json}"

executeVersionMigration() {
  if [ -n "${DB_PASSWORD_FILE:-}" ] && [ -r "$DB_PASSWORD_FILE" ]; then
    DB_PASSWORD="$(cat "$DB_PASSWORD_FILE")"
  fi
  echo "Execute schematron-validator-migration.sh"
  local output
  local script_rc=0
  output=$(/opt/startup-scripts/migration-scripts/schematron-validator-migration.sh \
    --src  "${SCHEMATRON_VALIDATOR_SOURCE_PATH}" \
    --dest "${XML_VALIDATION_PATH}" \
    --indexPath "${XML_VALIDATION_INDEX_PATH}" \
    --tabProfilesPath "${XML_VALIDATION_TABPROFILES_PATH}" \
    --db   "${DB_NAME}" \
    --host "${DB_HOST}" \
    --port "${DB_PORT}" \
    --user "${DB_USER}" \
    --password "${DB_PASSWORD}" 2>&1) || script_rc=$?
  printf '%s\n' "$output"

  if [ $script_rc -ne 0 ]; then
    echo "[migration] Script exited with code $script_rc (skip)"
    export MIGRATION_PERFORMED=false
    return 0
  fi

  # Extract migrated profiles count
  local profiles_added
  profiles_added=$(echo "$output" | awk -F': ' '/Migration done - profiles added/ {print $2}' | tail -n1)
  if [[ "$profiles_added" =~ ^[0-9]+$ ]] && [ "$profiles_added" -gt 0 ]; then
    export MIGRATION_PERFORMED=true
    echo "[migration] Profiles migrated: $profiles_added"
  else
    export MIGRATION_PERFORMED=false
    echo "[migration] No profiles migrated (count=$profiles_added); skip recording version"
  fi
  return 0
}

# Decide whether to attempt migration; never block app start
MISSING_VARS=()
for v in SCHEMATRON_VALIDATOR_SOURCE_PATH XML_VALIDATION_PATH XML_VALIDATION_INDEX_PATH DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD; do
  [ -z "${!v:-}" ] && MISSING_VARS+=("$v")
done

CAN_MIGRATE=true
case "${GZL_MIGRATION_ENABLED,,}" in
  false|0|no|n)
    echo "[migration] Skipping: disabled by GZL_MIGRATION_ENABLED=$GZL_MIGRATION_ENABLED"
    CAN_MIGRATE=false
    ;;
esac

if [ "$CAN_MIGRATE" = true ] && [ ${#MISSING_VARS[@]} -gt 0 ]; then
  echo "[migration] Skipping: missing vars: ${MISSING_VARS[*]}"
  CAN_MIGRATE=false
elif [ "$CAN_MIGRATE" = true ]; then
  if command -v psql >/dev/null 2>&1; then
    if ! PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -Atqc 'SELECT 1' | grep -qx 1; then
      echo "[migration] Skipping: cannot reach database $DB_HOST:$DB_PORT (user $DB_USER)"
      CAN_MIGRATE=false
    fi
  else
    echo "[migration] Skipping: psql client not available"
    CAN_MIGRATE=false
  fi
fi

if [ "$CAN_MIGRATE" = true ]; then
  performMigration ${PERSISTENT_VERSION_FILE} ${GZL_MIGRATION_REPEATABLE_MIGRATION_ENABLED} ${GZL_MIGRATION_VERSION_FOLDER}
else
  echo "[migration] Not performed; starting service without migration"
fi

# Start the application (native if present, otherwise JVM)
start_cmd=()
if [ -x /work/application ]; then
  start_cmd=(/work/application -Dquarkus.http.host=0.0.0.0)
else
  start_cmd=(java ${JAVA_OPTS:-} -jar /deployments/quarkus-run.jar)
fi

if [ "$(id -u)" -eq 0 ] && [ "${RUN_AS_ROOT}" != "true" ]; then
  if id -u "$APP_USER" >/dev/null 2>&1; then
    if command -v runuser >/dev/null 2>&1; then
      exec runuser -u "$APP_USER" -- "${start_cmd[@]}"
    elif command -v su >/dev/null 2>&1; then
      exec su -s /bin/sh -c 'exec "$@"' "$APP_USER" -- "${start_cmd[@]}"
    else
      echo "[startup] Warning: cannot drop privileges (runuser/su missing); running as root"
    fi
  else
    echo "[startup] Warning: user $APP_USER not found; running as root"
  fi
fi

exec "${start_cmd[@]}"
