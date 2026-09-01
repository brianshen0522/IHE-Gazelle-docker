# Copyright © 2024 KEREVAL SAS . All rights reserved.
#
# This object, including its structure, its organization, its documentation is the property of KEREVAL SAS . All rights reserved.
#
# Permission is granted to use this file solely within the recipient organization for internal purposes only.
# Any modification, redistribution, or use for commercial purposes without explicit written permission
# from KEREVAL SAS is strictly prohibited.
#
# No part of this material may be reproduced, distributed, or transmitted in any form, by any means, electronic or mechanical, including recording or other
# information storage and retrieval systems without the prior written authorization of KEREVAL SAS and may result in civil and/or criminal penalties.
#
# For permission requests, please contact: contact@kereval.com
#
# Internal Use Only

FROM rg.fr-par.scw.cloud/tools/keycloak:26.6.4

HEALTHCHECK --interval=5s --timeout=5s --start-period=25s --retries=5 CMD curl -f --insecure "https://127.0.0.1:9000/${KC_HTTP_RELATIVE_PATH}/health" || exit 1

USER root

ENV KC_METRICS_ENABLED=true
ENV KC_HEALTH_ENABLED=true
ENV KC_DB=postgres
ENV REALM=gazelle

RUN mkdir /opt/startup-scripts

WORKDIR /opt/keycloak
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048  \
    -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore

## Install CAS protocol extension
ADD "https://nexus.ihe-catalyst.net/repository/releases/org/keycloak/keycloak-protocol-cas/26.6.3/keycloak-protocol-cas-26.6.3.jar" /opt/keycloak/providers/gazelle-keycloak-cas-protocol.jar

## Install postgresql client
RUN apt-get update && apt-get install -y postgresql-client jq && apt-get clean

COPY realm-migration /realm-migration
ADD https://nexus.ihe-catalyst.net/repository/releases/org/keycloak/keycloak-config-cli/26.5.5/keycloak-config-cli-26.5.5.jar ./

## Install custom script for entrypoint
COPY run.sh /run.sh
RUN chmod +x /run.sh

# USER keycloak # VLD: problem with the user keycloak to write in /opt directory

COPY keycloak-migration.sh /opt/startup-scripts/keycloak-migration.sh
RUN chmod +x /opt/startup-scripts/keycloak-migration.sh

COPY gazelle-migration.sh /opt/startup-scripts/gazelle-migration.sh
RUN chmod +x /opt/startup-scripts/gazelle-migration.sh

## Copy custom user provider
COPY conf/quarkus.properties /opt/keycloak/conf/
COPY target/keycloak-provider-shaded.jar /opt/keycloak/providers/

## Pre-build keycloak
RUN /opt/keycloak/bin/kc.sh build

## Entrypoint
ENTRYPOINT ["/run.sh"]
