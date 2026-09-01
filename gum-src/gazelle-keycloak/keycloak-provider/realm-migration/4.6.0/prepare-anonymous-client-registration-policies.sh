#!/bin/bash

SERVER="http://localhost:${KC_HTTP_PORT}${KC_HTTP_RELATIVE_PATH:-/}"

deleteComponentByName() {
  POLICY_ID=$(/opt/keycloak/bin/kcadm.sh get components -r gazelle --server $SERVER -q name="$1" | jq -r '.[] | select(.subType=="anonymous") | .id' 2>/dev/null)
  echo "Deleting component id : $POLICY_ID - $1"
  /opt/keycloak/bin/kcadm.sh delete components/$POLICY_ID -r gazelle --server $SERVER || return 0
}

updateComponentByName() {
  POLICY_ID=$(/opt/keycloak/bin/kcadm.sh get components -r gazelle --server $SERVER -q name="$1" | jq -r '.[] | select(.subType=="anonymous") | .id' 2>/dev/null)
    echo "Updating component id : $POLICY_ID - $1 $2"
  /opt/keycloak/bin/kcadm.sh update components/$POLICY_ID -r gazelle --server $SERVER -s $2 || return 0
}

deleteComponentByName "Allowed Client Scopes"
updateComponentByName "Trusted Hosts" "config.trusted-hosts=[\"${FQDN:-localhost}\"]"
updateComponentByName "Trusted Hosts" "config.host-sending-registration-request-must-match=[\"false\"]"