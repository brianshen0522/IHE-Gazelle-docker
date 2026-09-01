#!/bin/bash

SERVER="http://localhost:${KC_HTTP_PORT}${KC_HTTP_RELATIVE_PATH:-/}"

updateComponentByName() {
  POLICY_ID=$(/opt/keycloak/bin/kcadm.sh get components -r gazelle --server $SERVER -q name="$1" | jq -r '.[] | select(.subType=="anonymous") | .id' 2>/dev/null)
    echo "Updating component id : $POLICY_ID - $1 $2"
  /opt/keycloak/bin/kcadm.sh update components/$POLICY_ID -r gazelle --server $SERVER -s $2 || return 0
}

updateComponentByName "Trusted Hosts" "config.trusted-hosts=[\"${FQDN:-localhost}\",\"127.0.0.1\"]"
updateComponentByName "Trusted Hosts" "config.host-sending-registration-request-must-match=[\"true\"]"