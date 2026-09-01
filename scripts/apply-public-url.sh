#!/bin/bash
# Apply the PUBLIC_* settings from .env to the whole stack.
# Usage: edit .env (PUBLIC_SCHEME / PUBLIC_HOST / PUBLIC_PORT / PUBLIC_URL /
#        PUBLIC_HOSTPORT), then run:  ./scripts/apply-public-url.sh
set -euo pipefail
cd "$(dirname "$0")/.."
set -a; source .env; set +a

echo "==> applying PUBLIC_URL=${PUBLIC_URL}"
case "$PUBLIC_URL" in
  "${PUBLIC_SCHEME}://${PUBLIC_HOST}"*) ;;
  *) echo "ERROR: PUBLIC_URL does not match PUBLIC_SCHEME://PUBLIC_HOST — fix .env"; exit 1 ;;
esac

# 1. Recreate containers whose environment changed (restart is NOT enough:
#    TM's afterMigrate.sql re-renders application preferences from env on boot)
docker compose up -d --remove-orphans

# NOTE: this stack never terminates TLS. If a public https URL is used, an
#       external reverse proxy does it and PUBLIC_HOST must resolve to that proxy
#       from INSIDE the containers too (real DNS) -- otherwise CAS/OIDC
#       backchannel calls break. See README "前端反向代理需求清單".

# 2. Wait for Keycloak
echo "==> waiting for Keycloak"
for i in $(seq 1 60); do
  docker exec gazelle-keycloak curl -sf "http://localhost:8180/auth/realms/gazelle/.well-known/openid-configuration" -o /dev/null && break
  sleep 5
done

# 3. Update the two browser-facing Keycloak clients (realm migrations only run
#    once per version, so URL changes must be pushed with kcadm)
KCADM="docker exec gazelle-keycloak /opt/keycloak/bin/kcadm.sh"
$KCADM config credentials --server http://localhost:8180/auth --realm master \
  --user "${KC_ADMIN_USER}" --password "${KC_ADMIN_PASSWORD}"

cid() { $KCADM get clients -r gazelle -q "clientId=$1" --fields id --format csv --noquotes; }

$KCADM update "clients/$(cid OIDC_GAZELLE_CLIENT)" -r gazelle \
  -s "rootUrl=${PUBLIC_URL}/" \
  -s "redirectUris=[\"${PUBLIC_URL}/*\",\"http://localhost*\"]" \
  -s "webOrigins=[\"${PUBLIC_URL}\",\"+\"]"
echo "==> OIDC_GAZELLE_CLIENT updated"

$KCADM update "clients/$(cid cas-gazelle-tm)" -r gazelle \
  -s "redirectUris=[\"${PUBLIC_URL}/tm/*\"]"
echo "==> cas-gazelle-tm updated"

# 4. Testing-session logos live in the TM database as absolute URLs
docker exec gazelle-tm-db psql -U "${DB_USER}" -d "${DB_TM_NAME}" -c \
  "UPDATE tm_testing_session SET logo_url = regexp_replace(logo_url, '^https?://[^/]+', '${PUBLIC_URL}') WHERE logo_url ~ '^https?://';" >/dev/null
echo "==> testing session logo URLs updated"

# 5. TM (JBoss) reads CAS config + preferences at boot; make sure it was recreated
echo "==> waiting for TM (this can take a few minutes on first deploy)"
for i in $(seq 1 120); do
  docker exec gazelle-nginx curl -sf -o /dev/null "http://jboss:8080/tm/" && { echo "==> TM is up"; break; }
  sleep 10
done

echo "==> done. Open: ${PUBLIC_URL}/gazelle/home"
