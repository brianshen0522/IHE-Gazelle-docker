#!/bin/bash

echo "Deleting old gazelle roles..."

SERVER="http://localhost:${KC_HTTP_PORT}${KC_HTTP_RELATIVE_PATH:-/}"

deleteRealmRoleByName() {
  /opt/keycloak/bin/kcadm.sh delete roles/$1 -r gazelle --server $SERVER
}

deleteRealmRoleByName "admin_role"
deleteRealmRoleByName "vendor_late_registration_role"
deleteRealmRoleByName "vendor_role"
deleteRealmRoleByName "vendor_admin_role"
deleteRealmRoleByName "user_role"
deleteRealmRoleByName "testing_session_admin_role"
deleteRealmRoleByName "tests_editor_role"
deleteRealmRoleByName "project-manager_role"
deleteRealmRoleByName "monitor_role"

exit 0
