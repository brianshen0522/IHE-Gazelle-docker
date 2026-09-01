# 02 — Installation

> The complete from-zero installation runbook. Verified on 2026-09-01 by wiping
> everything with `docker compose down -v` and reinstalling.

## Prerequisites

- Docker + Docker Compose v2 (verified locally with OrbStack; macOS/Linux both fine)
- A usable public hostname (production mode) or simply the local mode
  `gazelle.localhost:8888`
- This directory must be **complete**. Besides what git provides you need two extra
  steps after cloning:
  - **Download the binaries tarball** (`IHE-Gazelle-docker-binaries.tar.gz`, ~513MB)
    from Google Drive:
    <https://drive.google.com/file/d/1qVVdGawFnavEfpxlayp78yEbQDo8eGVg/view?usp=sharing>
    and extract it at the repo root:
    ```bash
    # browser: open the link above and click Download, save into the repo root; or CLI:
    pip install gdown && gdown 1qVVdGawFnavEfpxlayp78yEbQDo8eGVg
    tar xzf IHE-Gazelle-docker-binaries.tar.gz   # populates downloads/ and keycloak/keycloak-config-cli-26.5.5.jar
    ```
    It contains ~480MB of pre-fetched binaries (Zulu JDK 7, the JBoss EAP zip, the
    patched TM EAR, Keycloak-related jars… see volume 06) — **without it the
    jboss/keycloak images cannot be built**, and several upstream sources no longer
    exist, so do not delete your copy.
  - `mailpit/certs/` (test self-signed certificates; **not in git** — regenerate with
    the commands in volume 03 before first start)

  Already in git: `gateways/*/quarkus-app/` and `gum/quarkus-app/` (prebuilt Quarkus
  outputs) and `gui-src/` (the GUI is built from source and carries local patches).

## Create your own `.env` (first step after cloning)

The repo ships only a template — no real passwords:

```bash
cp .env.example .env
```

Then **replace every `change-me-*` placeholder**. What each credential is for:

| System | Username source | Password source | Notes |
|---|---|---|---|
| **Keycloak admin console** (`/auth/admin`) | `KC_ADMIN_USER` | `KC_ADMIN_PASSWORD` | master-realm admin, highest SSO privilege |
| **SSO clients admin account** | `SSO_CLIENTS_ADMIN_USER` | `SSO_CLIENTS_ADMIN_PASSWORD` | used by TM/GUM at startup to self-register OAuth clients; humans never log in with it |
| **PostgreSQL** | `DB_USER` | `DB_PASSWORD` | shared by all three databases |
| **SMTP** | `SMTP_USERNAME` | `SMTP_PASSWORD` | mail authentication (built-in mailpit or your real server) |
| **m2m secrets** | — | the seven `*_M2M_SECRET` values | service-to-service JWTs; give each its own random value |
| **GUI session key** | — | `NEXTAUTH_SECRET` | long random string |

> Generate random values with `openssl rand -base64 24` (one per variable — do not reuse).

### The SSO user account (the one you log in with daily)

The SSO user is **not in `.env`** — it lives in the GUM database and is created by the
final installation step, `bootstrap-admin.sh`. Pass your own identity via environment
variables:

```bash
ADMIN_EMAIL=you@example.com \
ADMIN_FIRST=Your ADMIN_LAST=Name \
ADMIN_PASSWORD='YourStrong.Password1' \
ORG_SHORT=MY-LAB ORG_NAME='My Lab' \
./scripts/bootstrap-admin.sh
```

Defaults when no variables are given (defined at the top of the script): the email falls
back to `SSO_ADMIN_EMAIL` from `.env`, the password to `GazelleAdmin.2026`, the
organization to `KEREVAL-LAB` / `Brian Test Lab` — **always override these outside a
throwaway dev environment**. Password policy: at least 8 characters with upper/lower
case, a digit and a special character.

Subsequent users are not created by the script: use the GUI's user management (Create
user) or the public registration page. Password changes go through the login page's
Forgot Password flow (requires working SMTP) or the Keycloak admin console.

### Pre-exposure security checklist

- [ ] `KC_ADMIN_PASSWORD` is not `admin` or a placeholder
- [ ] every `*_M2M_SECRET`, `NEXTAUTH_SECRET` and `SSO_CLIENTS_ADMIN_PASSWORD` is random
- [ ] `DB_PUBLISHED_PORT` (5433) is not exposed to untrusted networks (or remove the
      `ports` entry entirely)
- [ ] the bootstrap SSO admin password was customized
- [ ] SMTP points at a real server, `SMTP_TRUST_ALL=false`, `SMTP_MOCK_ENABLED=false`
- [ ] the `/mailpit/` route is removed from nginx or access-restricted (the inbox is
      readable by anyone)

## Choose the public address

Next, fill in the `PUBLIC_*` block in `.env` (pick one of the two modes; details in
volume 03):

| | Local http mode | Production mode (external TLS proxy) |
|---|---|---|
| PUBLIC_SCHEME | `http` | `https` |
| PUBLIC_HOST | `gazelle.localhost` | public domain (**no underscores**) |
| PUBLIC_PORT | `8888` | `443` |
| NGINX_NETWORK_ALIAS | same as PUBLIC_HOST | placeholder name (e.g. `gazelle-nginx-internal.localhost`) |
| Precondition | none | proxy on a **separate machine**, resolvable via real DNS from inside the containers |

⚠️ **On a fresh install `M2M_AUDIENCE` must equal `PUBLIC_URL`** (the realm's audience
mapper is created from the environment; if you later change the URL without rebuilding
the DB, this value stays with the frozen mapper instead).

## Installation steps

```bash
# 1. Build all images and start (first build takes minutes; the TM EAR
#    deployment another 2-4 minutes)
docker compose up -d --build

# 2. Wait for TM to finish deploying
docker compose logs -f jboss          # or: curl -s -o /dev/null -w '%{http_code}' localhost:8888/tm/
#    gazelle-gum crash-loops during this phase — NORMAL: its DB migration reads
#    TM's usr_users table; it recovers by itself once TM's schema exists

# 3. Fix the OIDC redirect URIs + logo URLs (REQUIRED on first install, not only
#    on URL changes: realm migration produces `https://host*`-style redirect URIs,
#    and Keycloak wildcards only match at the end of a PATH, so that form never
#    matches anything)
./scripts/apply-public-url.sh

# 4. Create the first admin + organization
./scripts/bootstrap-admin.sh
#    override with: ADMIN_EMAIL / ADMIN_FIRST / ADMIN_LAST / ADMIN_PASSWORD /
#                   ORG_SHORT / ORG_NAME
```

## Post-install verification checklist

| Check | Expected |
|---|---|
| `curl -s -o /dev/null -w '%{http_code}' ${PUBLIC_URL}/` | 302 (→ /gazelle/home) |
| `${PUBLIC_URL}/tm/home.seam` | 200, **not** a 302 to install/installation.seam |
| `${PUBLIC_URL}/auth/realms/gazelle/.well-known/openid-configuration` | issuer = `${PUBLIC_URL}/auth/realms/gazelle` |
| GUI Sign in → Keycloak login (a Terms-of-Service page appears on first login — Accept) | back on /gazelle/home with your name shown |
| TM's Log in (CAS) | no second password prompt (SSO), user + Administration menu top-right |
| `/gazelle/user-management/users` | lists the bootstrap admin |
| `/gazelle/validation-portal/profiles` | lists the demo validation profiles (≥2) |
| `${PUBLIC_URL}/mailpit/` | Mailpit inbox opens |
| Forgot-password flow (login page) | the email appears in mailpit |

## Common installation failures

If you get stuck, check volume 05; the three most frequent:

1. **TM deployment fails with IJ000453** → the PG password was stored as SCRAM (in
   theory `authdb/00-md5-password.sh` handles this automatically; if it still happens,
   run
   `docker exec gazelle-tm-db psql -U gazelle -d postgres -c "ALTER USER gazelle WITH PASSWORD '<DB_PASSWORD>';"`
   then `docker compose restart jboss`).
2. **bootstrap's organization step returns 401** → `M2M_AUDIENCE` does not match the
   realm mapper (see above).
3. **TM home still redirects to the install wizard after bootstrap** → the
   organization's MQTT event arrived before TM was ready;
   `docker compose restart jboss` and mosquitto will redeliver it.

## Full reset

```bash
docker exec gazelle-tm-db pg_dumpall -U gazelle > backup/$(date +%F).sql   # back up first
docker compose down -v      # stop and delete all data volumes
```

Then rerun the installation steps (with images already present, `--build` is mostly
cache hits and fast).
