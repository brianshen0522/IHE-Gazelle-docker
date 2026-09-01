# 03 — Configuration

> All system configuration is centralized in `.env`. After editing, run the apply action
> noted in each section.
> Golden rule: after changing env, `docker compose up -d` (recreate) — **`restart` does
> not apply new env**.

## Full `.env` reference

### Public address (`PUBLIC_*`)

| Variable | Meaning |
|---|---|
| `PUBLIC_SCHEME` | `http` or `https` |
| `PUBLIC_HOST` | Public hostname. **No underscores** (Java's `URI.getHost()` returns null → Keycloak NPEs on every request) |
| `PUBLIC_PORT` | Public port (443 in https production mode) |
| `PUBLIC_URL` | Composite value, must equal `SCHEME://HOST[:PORT]` (no port for 80/443) |
| `PUBLIC_HOSTPORT` | Same, host[:port] only |
| `NGINX_HTTP_PORT` | Host port for the stack's own nginx (default 8888) |
| `NGINX_NETWORK_ALIAS` | Which name gets aliased to the stack nginx inside the container network. **Local http mode**: set it equal to `PUBLIC_HOST` (containers and browsers then see the same URL). **Production mode**: set a useless placeholder (compose rejects empty aliases); the public hostname resolves via real DNS to the external proxy instead |

**Apply**: `./scripts/apply-public-url.sh` — recreates affected containers, uses kcadm to
update the two browser-facing clients' redirect URIs (`OIDC_GAZELLE_CLIENT`,
`cas-gazelle-tm`), aligns the realms' `sslRequired` with the scheme (see below),
rewrites the testing-session logo URLs, and waits for TM. Realm migrations only run
once per version, so URL changes must be pushed by this script.

**Plain-http public addresses** (e.g. `http://<vm-ip>:8888` reached from other
machines): Keycloak realms default to `sslRequired=EXTERNAL`, which rejects http
logins from non-private client IPs with an "HTTPS required" error page. The apply
script handles this automatically — `PUBLIC_SCHEME=http` sets `sslRequired=NONE` on
the master and gazelle realms, `https` sets it back to `EXTERNAL`. (Local
`gazelle.localhost` access never triggers it because the client IP is loopback.)

### Database

| Variable | Meaning |
|---|---|
| `DB_USER` / `DB_PASSWORD` | Shared credentials for all three databases (the password must be stored as **md5** — handled automatically at initdb time by `authdb/00-md5-password.sh`) |
| `DB_TM_NAME` | TM database name (`gazelle-tm`) |
| `DB_PUBLISHED_PORT` | PG port published to the host (5433) |

### Keycloak / SSO

| Variable | Meaning |
|---|---|
| `KC_ADMIN_USER` / `KC_ADMIN_PASSWORD` | Keycloak master-realm admin (⚠️ change before exposing the stack) |
| `SSO_CLIENTS_ADMIN_USER` / `_PASSWORD` | Account with manage-clients; TM/GUM use it to self-register clients |
| `SSO_ADMIN_EMAIL` | Admin email (bootstrap's default) |
| `M2M_AUDIENCE` | Audience of m2m tokens. **Must equal the realm's audience-mapper value**: on a fresh database the mapper is created from `ROOT_TEST_BED_URL` (= PUBLIC_URL), so set it to PUBLIC_URL; after changing the URL without rebuilding the DB, the mapper stays frozen and this value follows the mapper, not PUBLIC_URL |
| `*_M2M_SECRET` | Per-service m2m client secrets |
| `NEXTAUTH_SECRET` | GUI session JWT signing key |

### SMTP (one block for all three mail senders)

Keycloak (password reset / verification mail), GUM (registration / account mail) and TM
(notifications) all consume this block:

| Variable | Meaning |
|---|---|
| `SMTP_HOST` / `SMTP_PORT` | SMTP server (defaults to the built-in `mailpit:1025`) |
| `SMTP_USERNAME` / `SMTP_PASSWORD` | SMTP credentials |
| `SMTP_STARTTLS` | `true/false` — STARTTLS (consumed by the Keycloak realm and TM's JBoss) |
| `SMTP_STARTTLS_MODE` | The equivalent for GUM (Quarkus); keep in sync: `true→REQUIRED` / `false→DISABLED` |
| `SMTP_SSL` | `true/false` — implicit TLS / SMTPS (465); mutually exclusive with STARTTLS |
| `SMTP_FROM_DOMAIN` | Sender = `no-reply@<this domain>` (also written into TM's `application_no_reply_email`) |
| `SMTP_MOCK_ENABLED` | GUM mock mode (true = log only, never send); keep `false` with a real server |
| `SMTP_TRUST_ALL` | `true` for self-signed servers (the built-in mailpit); `false` for real servers with public CAs (affects GUM only; Keycloak/TM use the CA trust chain below) |

**Apply**: `docker compose up -d` (TM's mail configuration is re-rendered from env into
standalone.xml by `jboss/entrypoint.sh` on every boot — host/port, `<login>` credentials
and the `ssl`/`tls` attributes).

Self-signed trust chain (for the built-in mailpit):
- CA and server certificate live in `mailpit/certs/` (`ca.pem` / `cert.pem` / `key.pem`)
- Keycloak: `KC_TRUSTSTORE_PATHS` mounts ca.pem (merged with the system CAs — harmless
  for real servers)
- TM: the entrypoint imports ca.pem into the JRE cacerts via keytool
- GUM: `QUARKUS_MAILER_TRUST_ALL=${SMTP_TRUST_ALL}`

Regenerating the test certificates (required on fresh clones — the directory is not in
git):

```bash
mkdir -p mailpit/certs && cd mailpit/certs
openssl req -x509 -newkey rsa:2048 -keyout ca-key.pem -out ca.pem -days 3650 -nodes -subj "/CN=Gazelle Test SMTP CA"
openssl req -newkey rsa:2048 -keyout key.pem -out csr.pem -nodes -subj "/CN=mailpit"
openssl x509 -req -in csr.pem -CA ca.pem -CAkey ca-key.pem -CAcreateserial -out cert.pem -days 3650 \
  -extfile <(printf "subjectAltName=DNS:mailpit,DNS:localhost")
chmod 644 key.pem cert.pem && rm csr.pem
```

Verifying mail:
- Keycloak: login page → Forgot Password → check `${PUBLIC_URL}/mailpit/`
- GUM: register a new account → Account Activation mail
- TM: as admin, `/tm/administration/preferences.seam` → the envelope icon ("Test
  email") next to Admin Email

⚠️ Keycloak's realm SMTP is **re-applied on every boot** from
`keycloak/realm-migration/repeatable/smtp.json` — changes made in the admin console get
overwritten; edit `.env` instead.

### Miscellaneous

| Variable | Meaning |
|---|---|
| `TZ` | Container-wide timezone |

## TM application preferences

TM's Flyway treats **all environment variables** as placeholders; `afterMigrate.sql`
rewrites ~53 rows of `cmn_application_preference` from the jboss service's lowercase env
vars (`application_url`, `cas_enabled`, `default_gazelle_language`, …) **on every
start**.

- To change a preference → edit the jboss env in `docker-compose.yml` →
  `docker compose up -d jboss`
- Preferences changed in TM's admin pages are **overwritten on the next restart** —
  permanent changes always go through compose
- `default_gazelle_language` must be the language table's description value (`EN`);
  `English` breaks test creation with a NOT NULL violation

## Front TLS proxy requirements (production)

This stack never terminates TLS. The external proxy (on a **separate machine**) must:

| # | Requirement | Why |
|---|---|---|
| 1 | Forward to `http://<this host>:8888`, plain http upstream | this layer has no 443, no certificates |
| 2 | Pass `Host` unchanged (include the port when the public port is not 80/443) | Keycloak/NextAuth build URLs from it |
| 3 | Send `X-Forwarded-Proto: https` (ideally also `X-Forwarded-Port: 443`); **never send `X-Forwarded-Port: 8888`** | otherwise Keycloak's backchannel token endpoint leaks :8888 |
| 4 | `proxy_buffer_size 32k; proxy_buffers 8 32k; proxy_busy_buffers_size 64k;` | NextAuth's large Set-Cookie 502s on default buffers |
| 5 | `large_client_header_buffers 8 32k;` | Keycloak + NextAuth cookies exceed the 8k default → 400 (defaults drop the connection around 16k) |
| 6 | Pass through `Upgrade`/`Connection` (WebSocket) | the GUI and some backends use WS |
| 7 | `client_max_body_size ≥ 100m`, `proxy_read_timeout ≥ 300s` | validation uploads and slow TM requests |
| 8 | Public hostname without underscores | Java URI parsing |
| 9 | The public hostname must resolve **from inside the gazelle containers** (real DNS pointing at the proxy) | CAS validation / JWKS fetches go over the backchannel; internal and external URLs must be identical |
| 10 | Certificates, HSTS, http→https redirects are the proxy's job | this stack deliberately contains zero SSL configuration |

⚠️ When the TLS proxy runs on the **same machine** as the stack (common on dev
machines): containers cannot hairpin to the host's public IP — use local http mode
instead of modifying a shared proxy.

## JBoss reverse-proxy details (automated; for understanding only)

EAP 6.1 does not support `X-Forwarded-*`; `jboss/entrypoint.sh` rewrites the http
connector with `scheme/proxy-name/proxy-port/secure` (from `PUBLIC_*`) on every boot —
otherwise TM generates http:// CAS service URLs → Keycloak "Client not found". The CAS
configuration file `/opt/gazelle/cas/gazelle-tm.properties` is rendered from
`PUBLIC_URL` by the same entrypoint.
