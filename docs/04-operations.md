# 04 — Operations

## Day-to-day

```bash
docker compose up -d              # start (cold-start ordering is guaranteed by healthchecks)
docker compose ps                 # status
docker compose logs -f jboss      # TM deployment log (same for other services)
docker compose down               # stop (data preserved)
```

Rules:
- **After changing env → `up -d` (recreate), never `restart`** (restart keeps the old
  env, and TM's afterMigrate would rewrite preferences from it).
- **After any restart/recreate of the db container, `docker compose restart jboss`**
  (the EAP pool holds broken connections until restarted; Quarkus services recover on
  their own).
- **Portals go empty after restarting registry/keycloak alone** (services get kicked,
  registry logs Unauthorized every minute; upstream WebSocket-reconnect bug):
  `docker compose restart simulation-gateway validation-gateway xml-validation-service maestro gum`
- On macOS, editing a **single-file bind mount** (e.g. `nginx/default.conf`) changes the
  inode and the container keeps the stale file — always
  `docker compose up -d --force-recreate <service>`; a reload inside the container is
  useless.

## Account management

| Task | How |
|---|---|
| Add a user | GUI `/gazelle/user-management` (Create user) or the public registration page `/gazelle/user-management/registration` |
| Change a password (user) | Login page → Forgot Password (needs working SMTP), or the Keycloak admin console |
| Admin actions | `/gazelle/user-management/users`: enable/disable, roles, delete ("Delete user and associated data" also **archives** the user's organization; TM keeps its usr_institution row) |
| Keycloak admin console | `${PUBLIC_URL}/auth/admin/` (master realm admin; the business realm is `gazelle`) |
| Recreate the admin (fresh DB) | `./scripts/bootstrap-admin.sh` |

⚠️ After editing the GUM DB directly, Keycloak's federation cache may serve a stale
snapshot:
`docker exec gazelle-keycloak /opt/keycloak/bin/kcadm.sh create clear-user-cache -r gazelle`
(after `kcadm.sh config credentials --server http://localhost:8180/auth --realm master --user <KC_ADMIN_USER> --password <KC_ADMIN_PASSWORD>`).

## Backup and restore

```bash
# Back up (all three databases at once)
docker exec gazelle-tm-db pg_dumpall -U gazelle > backup/all-$(date +%F).sql

# Restore into a fresh volume (after down -v and up; restart the consumers afterwards)
cat backup/all-XXXX.sql | docker exec -i gazelle-tm-db psql -U gazelle -d postgres
docker compose restart jboss keycloak gum
```

`backup/` is git-ignored; keep dumps out of the repository.

## Validation profile management (Validation portal)

- Profile index: `xvs-resources/index.json`; XSD/Schematron files go under
  `xvs-resources/profiles/<name>/`
- After editing: `docker compose restart xml-validation-service`
- ⚠️ Every profile must set `schematronVersion` (even XSD-only profiles) — a missing
  value makes the Maestro step fail and the frontend hang on "Validating"
- Maestro's VALIDATION step requires the property name `contentToValidate`

## Simulator management (Simulation portal)

- Static demo: `sreg-config/services.json` (loaded by the registry at boot) +
  `static-sim/www/` (serves the sequences JSON at
  `{serviceUrl}/simulation/v1/sequences`)
- Add a real simulator by mirroring those two files; every registering service's
  `GZL_SERVICE_K8S_ID` (`name-instance-replica`) must be unique — duplicates overwrite
  each other in the registry

## Updating a single service

| Target | Steps |
|---|---|
| GUI (changes in gui-src) | `docker compose build gui && docker compose up -d gui` |
| A Quarkus service (rebuilt quarkus-app) | drop the new `quarkus-app/` into `gateways/<x>/` → `docker compose build <svc> && up -d <svc>` |
| TM (EAR / entrypoint / Dockerfile) | `docker compose build jboss && docker compose up -d jboss` (the entrypoint is baked into the image — changes require a rebuild) |
| Keycloak (provider jar / realm migration) | provider jars and the migration directory are bind mounts; `up -d --force-recreate keycloak` after editing; new migration versions go in `keycloak/realm-migration/<version>/` |
| nginx config | edit `nginx/default.conf` → `docker compose up -d --force-recreate nginx` |

## Mail inbox (development)

- Mailpit UI: `${PUBLIC_URL}/mailpit/`; API: `/mailpit/api/v1/messages`
- Switching to a real SMTP server: edit the `SMTP_*` block in `.env` (volume 03); the
  mailpit container can stay running, it just won't be used

## Quick health check

```bash
curl -s -o /dev/null -w '%{http_code}\n' localhost:8888/tm/home.seam        # 200
curl -s localhost:8888/auth/realms/gazelle/.well-known/openid-configuration | head -c 80
curl -s -o /dev/null -w '%{http_code}\n' localhost:8888/gazelle/home        # 200
curl -s localhost:8888/mailpit/api/v1/messages | head -c 40
docker compose ps   # everything Up; keycloak/db/registry/mailpit/jboss show (healthy)
```
