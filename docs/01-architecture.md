# 01 — Architecture

> This stack is a complete, modernized IHE Gazelle test bed: Test Management 12.1.0 +
> Gazelle User Management 5.0.1 + Keycloak SSO + portal backends (validation/simulation),
> all deployed with docker compose.

## Service inventory

| Service (compose name) | Container | Image | Stack | Internal port | Purpose |
|---|---|---|---|---|---|
| `nginx` | gazelle-nginx | nginx:alpine | — | 8888 (published) | **Single HTTP entry point**, path-based routing |
| `db` | gazelle-tm-db | postgres:17-alpine | PG 17 | 5432 (published 5433) | Single database container: `gazelle-tm` + `keycloak` + `gum` |
| `mqtt` | gazelle-tm-mqtt | eclipse-mosquitto:2 | — | 1883 | GUM→TM organization sync event bus |
| `keycloak` | gazelle-keycloak | gazelle-keycloak:26.6.4-jdk25 | KC 26.6.4 / JDK 25 | 8180 | SSO (OIDC + CAS plugin), realm `gazelle` |
| `gum` | gazelle-gum | gazelle-gum:5.0.1 | Quarkus / JDK 25 | 8081 | User Management API (users/organizations master data) |
| `gui` | gazelle-gui | gazelle-gui:4.0.0 | Next.js 15 | 3030 | Frontend: home, user management, validation/simulation portals, reports |
| `jboss` | gazelle-tm-app | gazelle-tm-jboss:12.1.0 | JBoss EAP 6.1 / Java 7 | 8080 | Test Management itself (EAR 12.1.0, amd64 emulation) |
| `service-registry` | gazelle-service-registry | gazelle-service-registry:2.1.0 | Quarkus / JDK 25 | 8097 | Service registry (source of the portals' service lists) |
| `validation-gateway` | gazelle-validation-gateway | gazelle-validation-gateway:1.0.2 | Quarkus / JDK 25 | 8092 | Validation request routing |
| `simulation-gateway` | gazelle-simulation-gateway | gazelle-simulation-gateway:0.3.2 | Quarkus / JDK 25 | 8087 | Simulation service routing |
| `xml-validation-service` | gazelle-xml-validation | gazelle-xml-validation-service:3.1.1 | Quarkus / JDK 25 | 8084 | XSD / Schematron validator |
| `maestro` | gazelle-maestro | gazelle-maestro:1.2.0 | Quarkus / JDK 25 | 8150 | Validation orchestrator (standalone mode) |
| `static-sim` | gazelle-static-sim | nginx:alpine | — | 80 | Static simulator sequences demo |
| `mailpit` | gazelle-mailpit | axllent/mailpit:v1.27 | — | 1025 / 8025 | Fake SMTP server (STARTTLS + auth enforced) + inbox UI |

**Apart from nginx's 8888 and the database's 5433, no service publishes a host port**;
browser traffic always flows `${PUBLIC_URL}` → external TLS proxy → this host's `:8888`
(nginx) → the container network.

## Traffic topology

```
                    ┌─ separate machine (production TLS proxy, terminates https)
Browser ── https ───┤
                    └─► http://<this host>:8888  gazelle-nginx (path routing)
                              │
     ┌───────┬─────────┬──────┼──────────┬──────────┬─────────┐
     ▼       ▼         ▼      ▼          ▼          ▼         ▼
   /tm     /auth    /gazelle /gum   /validation- /service-  /mailpit
  JBoss   Keycloak   GUI     GUM     gateway etc  registry   Mailpit
 (TM 12) (SSO/CAS) (Next.js)(API)   (Quarkus)
```

Path routing table (`nginx/default.conf`):

| Path | Upstream |
|---|---|
| `/` | 302 → `/gazelle/home` (relative redirect, `absolute_redirect off`) |
| `/tm`, `/GAZELLE_LOGO.gif` | jboss:8080 |
| `/gazelle` | gui:3030 |
| `/auth` | keycloak:8180 (`KC_HTTP_RELATIVE_PATH=/auth`) |
| `/gum` | gum:8081 |
| `/validation-gateway` `/simulation-gateway` `/service-registry` `/maestro` `/xml-validation-service` | the matching Quarkus service |
| `/static-simulator/` | static-sim:80 |
| `/mailpit` | mailpit:8025 |

## Authentication (SSO) data flow

- **GUI (Next.js)**: NextAuth → Keycloak **OIDC** (client `OIDC_GAZELLE_CLIENT`, PKCE).
- **TM (JBoss)**: **CAS 3.0** → Keycloak's CAS protocol plugin (client `cas-gazelle-tm`).
  On startup TM self-registers its CAS and m2m clients using the `gazelle-clients-admin`
  account.
- **Service-to-service (m2m)**: client_credentials JWTs. Each service self-registers its
  m2m client at startup; the token's `aud` is decided by the realm's audience mapper
  (created from `ROOT_TEST_BED_URL` at realm-migration time), and `.env`'s
  `M2M_AUDIENCE` must match that mapper.
- **User storage**: Keycloak stores no users itself — the gazelle user-storage provider
  reads/writes the **GUM database** directly (`gum."user"`; roles = rows in
  `gum.user_group`, e.g. `role:gazelle_admin`).
  ⚠️ Keycloak has a user-federation cache; after editing the GUM DB directly, run
  `kcadm create clear-user-cache -r gazelle`.
- **Single-issuer principle**: browsers and containers see the **exact same**
  `${PUBLIC_URL}/auth/realms/gazelle`. Inside containers the public hostname resolves via
  real DNS to the external proxy (production mode) or via a compose network alias to the
  stack's own nginx (local http mode).

## Organization / user sync

```
GUM (create organization) ─ MQTT topic: organization-management ─► TM subscriber
                                                                    └► creates usr_institution
```

- TM's "should the install wizard run?" check = does the TM DB contain at least one
  organization.
- mosquitto redelivers unacknowledged events after a TM restart (the self-healing path
  when TM missed an event).

## Validation chain

```
Validation portal (GUI) → validation-gateway → maestro → xml-validation-service
                                                          (XSD / Schematron)
```

- Maestro runs in **standalone mode** (`GZL_REPORT_RECORDING_ENABLED=false`): reports are
  not persisted (Datahouse is not deployed, see volume 06) and the GUI renders the
  returned report inline.
- Validation profiles live in `xvs-resources/index.json` + `profiles/` (XSD/Schematron
  files).

## Service registration (where the portal lists come from)

Every Quarkus service self-registers with the service-registry over WebSocket at startup;
the registry additionally loads static entries from `sreg-config/services.json` (the demo
simulator).

- Every registering service needs a **globally unique** `GZL_SERVICE_K8S_ID`
  (`name-instance-replica`).
- Cold-start ordering is guaranteed by compose healthchecks: keycloak realm ready →
  registry healthy → everything else.

## SMTP

All three mail senders (Keycloak, GUM, TM) share the `SMTP_*` block in `.env`; see
volume 03. The default points at the built-in mailpit (STARTTLS + credentials enforced,
inbox at `${PUBLIC_URL}/mailpit/`).

## Data persistence (volumes)

| Volume | Contents | Consequence of deleting |
|---|---|---|
| `pgdata17` | all three databases (gazelle-tm / keycloak / gum) | every account, test datum and realm state is gone; rerun bootstrap |
| `gazelle-opt` | TM's `/opt/gazelle` (reports, CAS config, …) | entrypoint reseeds it from the image |
| `kc-app-version` | realm-migration version marker | migrations rerun (idempotent) |
