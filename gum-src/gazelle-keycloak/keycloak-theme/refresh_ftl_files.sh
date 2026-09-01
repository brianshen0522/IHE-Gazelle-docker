#!/bin/bash

export KEYCLOAK_VERSION=26.3.2
wget -O /tmp/keycloak-themes.jar "https://repo1.maven.org/maven2/org/keycloak/keycloak-themes/$KEYCLOAK_VERSION/keycloak-themes-$KEYCLOAK_VERSION.jar"

unzip -p /tmp/keycloak-themes.jar theme/base/login/login.ftl > src/main/resources/theme/gazelle/login/login.ftl
unzip -p /tmp/keycloak-themes.jar theme/base/login/login.ftl > src/main/resources/theme/gazelle-admin/login/login.ftl
unzip -p /tmp/keycloak-themes.jar theme/base/login/template.ftl > src/main/resources/theme/gazelle/login/template.ftl
unzip -p /tmp/keycloak-themes.jar theme/base/login/template.ftl > src/main/resources/theme/gazelle-admin/login/template.ftl
unzip -p /tmp/keycloak-themes.jar theme/base/login/terms.ftl > src/main/resources/theme/gazelle/login/terms.ftl
unzip -p /tmp/keycloak-themes.jar theme/keycloak/welcome/index.ftl > src/main/resources/theme/gazelle/welcome/index.ftl

echo "[WARNING] The index.ftl file for the admin console is not available in the keycloak-themes.jar file. Please copy it manually from the keycloak source code (available in target/classes/theme/keycloak.v2/admin/index.ftl) ."
#unzip -p /tmp/keycloak-themes.jar theme/keycloak.v2/admin/index.ftl > src/main/resources/theme/gazelle/admin/index.ftl
