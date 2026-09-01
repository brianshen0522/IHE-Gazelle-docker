#!/bin/bash

echo "Deleting m2m client scopes..."
SERVER="http://localhost:${KC_HTTP_PORT}${KC_HTTP_RELATIVE_PATH:-/}"
CLIENTSCOPES=$(/opt/keycloak/bin/kcadm.sh get client-scopes --server $SERVER -r gazelle --fields id,name)

deleteClientScopeByName() {
  ID_CLIENTSCOPE=$(echo $CLIENTSCOPES | jq -c '.[] | select(.name == "'$1'")' | jq -r '.id')
  if [ -z "$ID_CLIENTSCOPE" ]; then
    echo "Client scope $1 not found"
  else
    /opt/keycloak/bin/kcadm.sh delete client-scopes/$ID_CLIENTSCOPE -r gazelle --server $SERVER
  fi
}

deleteClientScopeByName "any-users:read-public"
deleteClientScopeByName "any-users:create"
deleteClientScopeByName "any-users:update"
deleteClientScopeByName "any-orga:read-private"
deleteClientScopeByName "any-orga:read-public"
deleteClientScopeByName "any-orga:create"
deleteClientScopeByName "any-orga:update"
deleteClientScopeByName "any-roles:read-private"
deleteClientScopeByName "my-orga:create"
deleteClientScopeByName "my-orga:read"
deleteClientScopeByName "my-orga:update"