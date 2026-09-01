#!/bin/bash

export KEYCLOAK_VERSION=26.5.3
docker run \
   -p 8080:8080 \
   --name keycloak-testing-container \
   -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
   -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
   -v `pwd`/src/main/resources/theme/gazelle:/opt/keycloak/themes/gazelle:rw \
   -it --rm "quay.io/keycloak/keycloak:$KEYCLOAK_VERSION" \
   start-dev --spi-theme-static-max-age=-1 --spi-theme-cache-themes=false --spi-theme-cache-templates=false
