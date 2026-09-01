#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Mapping variables between Gazelle and Keycloak
export KC_DB_URL=${DB_KC_JDBC_URL:-"jdbc:postgresql://${DB_KC_HOST:-localhost}:${DB_KC_PORT:-5432}/${DB_KC_NAME:-keycloak}"}
export KC_DB_USERNAME="${DB_KC_USER}"
export KC_DB_PASSWORD="${DB_KC_PASSWORD}"

##
# Database management
##
if [ "${DB_ENABLED:-true}" = "true" ]; then
    export PGPASSWORD=${DB_KC_PASSWORD}

    # Check if db host is reachable
    while ! pg_isready -h $DB_KC_HOST --port ${DB_KC_PORT} >/dev/null 2> /dev/null;
    do
      echo "Waiting 5s for database server to be reachable"
      sleep 5
    done
    echo -e "${GREEN}✔ Database server is accessible.${NC}"

    # Check if Keycloak database is created
    psql -h ${DB_KC_HOST} --port ${DB_KC_PORT} -U ${DB_KC_USER} ${DB_KC_NAME} -c '\q' 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "Creating ${DB_KC_NAME} database..."
        createdb -U ${DB_KC_USER} --port ${DB_KC_PORT} -h ${DB_KC_HOST} -E UTF-8 ${DB_KC_NAME}
    fi

    # Check if GUM database is created
    psql -h ${DB_GUM_HOST} --port ${DB_GUM_PORT} -U ${DB_GUM_USER} ${DB_GUM_NAME} -c '\q' 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "Creating ${DB_GUM_NAME} database..."
        createdb -U ${DB_GUM_USER} --port ${DB_GUM_PORT} -h ${DB_GUM_HOST} -E UTF-8 ${DB_GUM_NAME}
    fi
fi
echo "Waiting 5s for database server to be ready"
sleep 5

##
# Retrieve external provider jars comma separated list
##
if [ -n "${GZL_EXTERNAL_PROVIDER_JAR_URLS}" ]; then
  for gzlExternalProviderJarUrl in $(echo "${GZL_EXTERNAL_PROVIDER_JAR_URLS}" | tr "," "\n"); do
    # If the url start with http(s) we download the jar
    if [[ "${gzlExternalProviderJarUrl}" =~ ^http(s)?:// ]]; then
      echo "Download external provider jar from ${gzlExternalProviderJarUrl}..."
      wget -q -P /opt/keycloak/providers/ "${gzlExternalProviderJarUrl}"
      if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to download external provider $(basename "${gzlExternalProviderJarUrl}").${NC}"
        exit 1
      else
        echo -e "${GREEN}✔ External provider $(basename "${gzlExternalProviderJarUrl}") downloaded.${NC}"
      fi
    # Else we copy the jar from the local filesystem
    else
      echo "Copy external provider jar from ${gzlExternalProviderJarUrl}..."
      cp "${gzlExternalProviderJarUrl}" /opt/keycloak/providers/
      if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to copy external provider $(basename "${gzlExternalProviderJarUrl}").${NC}"
        exit 1
      else
        echo -e "${GREEN}✔ External provider $(basename "${gzlExternalProviderJarUrl}") copied.${NC}"
      fi
    fi
  done
fi


##
# Start Keycloak realm migrations
##
/opt/startup-scripts/keycloak-migration.sh &

##
# Start Keycloak
##
if [ "$DEV_MODE" == "true" ]; then
  /opt/keycloak/bin/kc.sh -v start-dev --import-realm --spi-theme-welcome-theme=gazelle --proxy-headers xforwarded
else
  /opt/keycloak/bin/kc.sh -v start --import-realm --spi-theme-welcome-theme=gazelle --proxy-headers xforwarded
fi