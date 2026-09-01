#!/bin/bash
# Patch EAP 6.1 standalone.xml for Gazelle TM, producing standalone.xml.template
# (entrypoint.sh substitutes @DB_*@ tokens at container start).
set -e
XML="$1"

DS='<datasource jta="true" jndi-name="java:jboss/datasources/TestManagementDS" pool-name="TestManagementDS" enabled="true" use-java-context="true">\
                    <connection-url>jdbc:postgresql://@DB_HOST@:@DB_PORT@/@DB_NAME@</connection-url>\
                    <driver>postgresql</driver>\
                    <security><user-name>@DB_USER@</user-name><password>@DB_PASSWORD@</password></security>\
                    <pool><min-pool-size>2</min-pool-size><max-pool-size>50</max-pool-size><prefill>false</prefill><use-strict-min>false</use-strict-min><flush-strategy>FailingConnectionOnly</flush-strategy></pool>\
                    <validation><check-valid-connection-sql>select 1</check-valid-connection-sql><validate-on-match>false</validate-on-match><background-validation>false</background-validation><use-fast-fail>false</use-fast-fail></validation>\
                    <timeout><idle-timeout-minutes>10</idle-timeout-minutes><blocking-timeout-millis>30000</blocking-timeout-millis></timeout>\
                    <statement><prepared-statement-cache-size>30</prepared-statement-cache-size><track-statements>false</track-statements></statement>\
                </datasource>'

DRV='<driver name="postgresql" module="org.postgresql">\
                        <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>\
                    </driver>'

# Insert datasource right after the opening <datasources> tag
sed -i "s|<datasources>|<datasources>\\
                ${DS}|" "$XML"

# Insert driver right after the opening <drivers> tag
sed -i "s|<drivers>|<drivers>\\
                    ${DRV}|" "$XML"

# Give slow first-boot migrations time to finish
sed -i 's|<deployment-scanner |<deployment-scanner deployment-timeout="1800" |' "$XML"
sed -i 's|<coordinator-environment default-timeout="300"/>|<coordinator-environment default-timeout="1800"/>|' "$XML"

grep -q 'TestManagementDS' "$XML" || { echo "datasource insertion failed"; exit 1; }
grep -q 'org.postgresql' "$XML" || { echo "driver insertion failed"; exit 1; }

cp "$XML" "${XML}.template"
echo "standalone.xml patched OK"
