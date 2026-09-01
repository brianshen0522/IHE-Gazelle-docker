#!/bin/bash

ENV_FILE=../../../.env-migration
cp $ENV_FILE .env-migration
# Remove start and end quotes from env variables in .env-migration file
sed -i 's/="/=/' .env-migration
sed -i 's/"$//' .env-migration
sed -i 's/^#.*$//' .env-migration
export $(cat .env-migration| xargs)

# Check target folder exists
PATH_TO_MIGRATION_FOLDER=$1
if [ ! -d "${PATH_TO_MIGRATION_FOLDER}" ]; then
    echo "No migration files found for version ${PATH_TO_MIGRATION_FOLDER}"
    exit 1
fi

docker run \
    --env-file .env-migration \
    -e KEYCLOAK_URL="http://localhost:${KC_HTTP_PORT}${KC_HTTP_RELATIVE_PATH:-/}" \
    -e KEYCLOAK_USER="${KC_BOOTSTRAP_ADMIN_USERNAME}" \
    -e KEYCLOAK_PASSWORD="${KC_BOOTSTRAP_ADMIN_PASSWORD}" \
    -e KEYCLOAK_AVAILABILITYCHECK_ENABLED=true \
    -e KEYCLOAK_AVAILABILITYCHECK_TIMEOUT=60s \
    -e IMPORT_FILES_LOCATIONS="/realm-migration/*.json" \
    -e IMPORT_VARSUBSTITUTION_ENABLED=true \
    -e IMPORT_MANAGED_CLIENTSCOPE=no-delete \
    -e IMPORT_MANAGED_CLIENT=no-delete \
    -e IMPORT_MANAGED_IDENTITYPROVIDER=no-delete \
    -e IMPORT_MANAGED_AUTHENTICATIONFLOW=no-delete \
    -e IMPORT_MANAGED_ROLE=no-delete \
    -e DEBUG=false \
    -v "${PATH_TO_MIGRATION_FOLDER}":/realm-migration \
    --network host \
    adorsys/keycloak-config-cli:6.5.0-26.5.4

# Retrieve all .sh files in the migration folder and execute them
DOCKER_CONTAINER_NAME="gazelle-user-management-keycloak"
docker cp ${PATH_TO_MIGRATION_FOLDER} ${DOCKER_CONTAINER_NAME}:/realm-migration/
docker exec -it ${DOCKER_CONTAINER_NAME} /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:${KC_HTTP_PORT}${KC_HTTP_RELATIVE_PATH:-/} --realm master --user ${KC_BOOTSTRAP_ADMIN_USERNAME} --password ${KC_BOOTSTRAP_ADMIN_PASSWORD}

for fileName in ${PATH_TO_MIGRATION_FOLDER}/*.sh; do
    docker exec -it ${DOCKER_CONTAINER_NAME} "/realm-migration/$fileName"
done


