#!/bin/bash
# Wrapper entrypoint for the official Keycloak image: launches the Gazelle
# realm migration in the background, then starts Keycloak in dev mode.
export KC_DB_URL=${DB_KC_JDBC_URL:-"jdbc:postgresql://${DB_KC_HOST:-authdb}:${DB_KC_PORT:-5432}/${DB_KC_NAME:-keycloak}"}
export KC_DB_USERNAME="${DB_KC_USER:-gazelle}"
export KC_DB_PASSWORD="${DB_KC_PASSWORD:-gazelle}"

cd /opt/keycloak
/opt/startup-scripts/keycloak-migration.sh &

exec /opt/keycloak/bin/kc.sh start-dev --http-host=0.0.0.0 --http-port="${KC_HTTP_PORT:-8180}" --proxy-headers xforwarded --spi-theme-welcome-theme=gazelle
