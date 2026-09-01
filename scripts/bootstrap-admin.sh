#!/bin/bash
# Bootstrap the first Gazelle admin + organization on a FRESH database
# (only needed after `docker compose down -v`). Run from anywhere once all
# services are up (TM deployed, Keycloak realm migrated) — typically right
# after ./scripts/apply-public-url.sh on a fresh install.
set -euo pipefail
cd "$(dirname "$0")/.."
set -a; source .env; set +a

ADMIN_EMAIL="${ADMIN_EMAIL:-${SSO_ADMIN_EMAIL}}"
ADMIN_FIRST="${ADMIN_FIRST:-Brian}"
ADMIN_LAST="${ADMIN_LAST:-Shen}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-GazelleAdmin.2026}"
ORG_SHORT="${ORG_SHORT:-KEREVAL-LAB}"
ORG_NAME="${ORG_NAME:-Brian Test Lab}"

KCADM="docker exec gazelle-keycloak /opt/keycloak/bin/kcadm.sh"
DB="docker exec gazelle-tm-db psql -U ${DB_USER}"

echo "== 1. Create user in Keycloak (stored via gazelle provider into GUM DB)"
$KCADM config credentials --server http://localhost:8180/auth --realm master \
  --user "${KC_ADMIN_USER}" --password "${KC_ADMIN_PASSWORD}" >/dev/null
$KCADM create users -r gazelle -s username=admin -s enabled=true \
  -s "email=${ADMIN_EMAIL}" -s "firstName=${ADMIN_FIRST}" -s "lastName=${ADMIN_LAST}" \
  -s emailVerified=true || true

USER_ID=$($DB -d gum -Atc "SELECT id FROM gum.\"user\" WHERE email='${ADMIN_EMAIL}'")
[ -n "$USER_ID" ] || { echo "ERROR: user not found in GUM DB"; exit 1; }
echo "   user id: ${USER_ID}"

echo "== 2. Set password"
KC_UID=$($KCADM get users -r gazelle -q "email=${ADMIN_EMAIL}" --fields id --format csv --noquotes)
$KCADM update "users/${KC_UID}/reset-password" -r gazelle \
  -s type=password -s "value=${ADMIN_PASSWORD}" -s temporary=false -n

echo "== 3. Grant gazelle_admin group in GUM DB"
$DB -d gum -c "INSERT INTO gum.user_group (user_id, group_id) VALUES ('${USER_ID}','role:gazelle_admin') ON CONFLICT DO NOTHING;" >/dev/null

echo "== 4. Create organization via GUM API (syncs to TM over MQTT)"
# Token must be requested through the PUBLIC URL so its issuer matches what GUM
# verifies (KC_HOSTNAME_BACKCHANNEL_DYNAMIC makes internal-URL tokens unusable).
TOK=$(curl -sk -X POST "${PUBLIC_URL}/auth/realms/gazelle/protocol/openid-connect/token" \
  -d "grant_type=client_credentials&client_id=m2m-gazelle-tm&client_secret=${TM_M2M_SECRET}" \
  | python3 -c 'import json,sys; print(json.load(sys.stdin).get("access_token",""))')
[ -n "$TOK" ] || { echo "ERROR: could not obtain m2m token"; exit 1; }
curl -sk -H "Authorization: Bearer ${TOK}" -H 'Content-Type: application/json' -X POST \
  -d "{\"shortname\":\"${ORG_SHORT}\",\"name\":\"${ORG_NAME}\"}" \
  "${PUBLIC_URL}/gum/rest/organizations"
echo

echo "== 5. Attach admin to organization"
ORG_ID=$($DB -d gum -Atc "SELECT id FROM gum.organization WHERE shortname='${ORG_SHORT}'")
[ -n "$ORG_ID" ] || { echo "ERROR: organization not found"; exit 1; }
$DB -d gum -c "UPDATE gum.\"user\" SET organization_id='${ORG_ID}' WHERE id='${USER_ID}';" >/dev/null

echo "== 6. Clear Keycloak user cache (federation cache still holds the pre-attach snapshot)"
$KCADM create clear-user-cache -r gazelle

echo "Done. Log in at ${PUBLIC_URL}/tm with ${ADMIN_EMAIL} / ${ADMIN_PASSWORD}"
