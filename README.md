# IHE Gazelle Test Bed — Docker Deployment

A complete, modernized IHE Gazelle test bed running entirely in Docker:

- **Test Management 12.1.0** (JBoss EAP 6.1 / Java 7, emulated amd64)
- **Gazelle User Management 5.0.1** + **Keycloak 26.6.4 SSO** (OIDC + CAS)
- **Gazelle User Interface 4.0.0** (Next.js: home, user management, validation/simulation portals)
- Portal backends: service-registry, validation/simulation gateways, maestro,
  xml-validation-service (end-to-end XML validation works out of the box)
- Built-in fake SMTP server (mailpit, STARTTLS + auth enforced) and a single nginx entry point

## 📖 Documentation

| Volume | Contents |
|---|---|
| [01 Architecture](docs/01-architecture.md) | Service inventory, traffic topology, SSO/MQTT/validation data flows, volumes |
| [02 Installation](docs/02-installation.md) | Prerequisites, fresh-install runbook, verification checklist, full reset |
| [03 Configuration](docs/03-configuration.md) | Full `.env` reference, public URL switching, SMTP, TM preferences, TLS proxy requirements |
| [04 Operations](docs/04-operations.md) | Day-to-day commands, account management, backup/restore, validation profiles / simulators, updating services |
| [05 Troubleshooting](docs/05-troubleshooting.md) | Symptom-indexed tables (login, URLs, TM, GUM, portals, SMTP, infrastructure) |
| [06 Sources & Patches](docs/06-sources-and-patches.md) | Artifact origins, `downloads/` inventory, local patches (read before upgrading anything) |

## Quick start

Day-to-day:

```bash
docker compose up -d          # start everything (first TM deployment takes 2-4 minutes)
docker compose ps             # status
docker compose logs -f jboss  # follow the TM deployment log
```

**Fresh install / reinstall** (first deployment, or after `docker compose down -v`;
see the [installation manual](docs/02-installation.md) for details):

```bash
# first clone only: download IHE-Gazelle-docker-binaries.tar.gz (~513MB) from
# Google Drive (link in docs/02-installation.md) and extract it at the repo root:
tar xzf IHE-Gazelle-docker-binaries.tar.gz
cp .env.example .env                # replace every change-me-* placeholder
                                    # (credential guide: installation manual, "Create your own .env")
docker compose up -d --build        # build all images and start
docker compose logs -f jboss        # wait for TM (gum crash-loops meanwhile — normal, self-heals)
./scripts/apply-public-url.sh       # fix OIDC redirect URIs (required on first install)
ADMIN_EMAIL=you@example.com ADMIN_PASSWORD='YourStrong.Password1' \
./scripts/bootstrap-admin.sh        # create your own SSO admin + organization
```

## Entry points and accounts

**Single entry point: `${PUBLIC_URL}`** (external TLS proxy → this host's `:8888`
served by gazelle-nginx; individual services expose no host ports).

| Service | Path | Credentials |
|---|---|---|
| Home (GUI) | `/` (→ `/gazelle/home`) | SSO |
| Gazelle TM | `/tm` | SSO admin created by bootstrap |
| Keycloak admin console | `/auth/admin/` | `KC_ADMIN_USER` / `KC_ADMIN_PASSWORD` from `.env` ⚠️ change before exposing |
| User management | `/gazelle/user-management/users` | SSO (gazelle_admin) |
| Validation portal | `/gazelle/validation-portal/profiles` | SSO |
| Simulation portal | `/gazelle/simulation-portal/sequences` | SSO |
| Mailpit inbox | `/mailpit/` | — |
| GUM / Registry / Gateways / Maestro / XVS APIs | `/gum` `/service-registry` `/simulation-gateway` `/validation-gateway` `/maestro` `/xml-validation-service` | m2m JWT |
| TM PostgreSQL | `localhost:5433` (only direct port) | `DB_USER` / `DB_PASSWORD` from `.env` |

## The three most important operational rules

1. **After changing env, use `docker compose up -d` (recreate), never `restart`** —
   restart does not apply new env, and TM would rewrite its preferences from the old env.
2. **After any restart of the db container, run `docker compose restart jboss`** —
   the EAP connection pool does not recover on its own.
3. **If the portals go empty after restarting registry/keycloak alone** →
   `docker compose restart simulation-gateway validation-gateway xml-validation-service maestro gum`.

## Known limitations

- Datahouse / EVSClient / Proxy are not deployed (official distribution channels are
  private); the GUI reports page is unusable, validation reports render inline instead.
  Evaluation notes in [06](docs/06-sources-and-patches.md).
- Java 7 + JBoss AS 7.2 is a hard upstream requirement of TM; runs under amd64
  emulation on Apple Silicon.
- MQTT retained messages replay organization:created on TM restart; TM logs one
  "already exists" error — harmless.
- Fresh clones must download the binaries tarball from Google Drive (link in
  [02](docs/02-installation.md)) and regenerate `mailpit/certs/` (commands in
  [03](docs/03-configuration.md)).
