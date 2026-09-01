# 06 — Sources & Local Patches

> This volume answers two questions: where every image's contents **come from**, and
> what we **changed relative to upstream**. Before upgrading any component, walk through
> the local-patch list and decide which patches must carry over.

## Artifact origins

| Source | Used for |
|---|---|
| `https://nexus.ihe-catalyst.net/repository/releases` (search API `/service/rest/v1/search`) | Gazelle's official Maven repo: TM EAR, proxy, datahouse and other releases |
| `https://gitlab.inria.fr/gazelle/public/core/*` | Public source code (REST API and git clone work; **the web UI sits behind Anubis anti-bot**) |
| `https://download.jboss.org/jbosseap/6/jboss-eap-6.1.0.Alpha/jboss-eap-6.1.0.Alpha.zip` | JBoss EAP 6.1.0.Alpha (= AS 7.2) binary |
| Azul CDN (cdn.azul.com) | Zulu JDK 7 tarball (Docker Hub's openjdk:7 images were purged) |
| ⚠️ Dead sources | the old `gazelle.ihe.net/nexus` and `/jboss7/*.zip` no longer exist |
| ⚠️ Private (unavailable) | `gazelle/private/industrialization/docker/*` (the official docker deployment projects), the Scaleway registry `rg.fr-par.scw.cloud/gazelle-snapshot` (anonymous pulls rejected), and runnable distributions/source of Datahouse and the new Proxy |

## `downloads/` inventory (~480MB, required for builds — distributed via Google Drive, see volume 02)

| File | Purpose |
|---|---|
| `zulu7.56.0.11-ca-jdk7.0.352-linux_x64.tar.gz` | TM's Java 7 runtime |
| `jboss-eap-6.1.0.Alpha.zip` | TM's application server |
| `gazelle-tm-ear-12.1.0.ear` / `-dist.zip` / `-sql.zip` / `-datasource.zip` | Official TM artifacts |
| **`gazelle-tm-ear-12.1.0-patched.ear`** | **The EAR actually deployed (carries the local patches below)** |
| `postgresql-42.2.1.jre7.jar` | Last PG JDBC driver supporting Java 7 |
| `javax.mail-1.6.2.jar` | Replaces EAP's bundled mail-1.4.5 (see patches) |
| `keycloak-protocol-cas-26.6.3.jar`, `keycloak-provider-5.0.1.jar`, `keycloak-theme-5.0.1.jar` | Keycloak CAS plugin, gazelle user-storage provider, theme |

(`keycloak/keycloak-config-cli-26.5.5.jar` — the realm-migration runner — is also part
of the binaries tarball, mounted from `keycloak/`.)

## Source trees (`*-src/`) and build outputs

| Directory | Upstream repo (gitlab.inria.fr/gazelle/…) | Fork-base commit | Build output |
|---|---|---|---|
| `gui-src/` | public/core/gazelle-user-interface (4.0.0) | `68b546a` | built directly by compose (`docker compose build gui`) |
| `gum-src/` | public/core/user-management (5.0.1) | `9782593` | `gum/quarkus-app/` |
| `sreg-src/` | public/core/service-registry (2.1.0) | `54f4448` | `gateways/sreg/quarkus-app/` |
| `vgw-src/` | public/core/validation-gateway (1.0.2) | `48cb742` | `gateways/vgw/quarkus-app/` |
| `sgw-src/` | public/core/simulation-gateway (0.3.2) | `37486c0` | `gateways/sgw/quarkus-app/` |
| `xvs-src/` | public/validation/xml-validation-service (3.1.1) | `dddd8fe` | `gateways/xvs/quarkus-app/` |
| `maestro-src/` | public/core/maestro (1.2.0) | `d76d3bb` | `gateways/maestro/quarkus-app/` |

> The `*-src/` trees were upstream clones; their embedded `.git` directories were removed
> when this repo was created (patches are vendored directly). To diff against upstream,
> re-clone at the commit above and compare.

Rebuilding a Quarkus service: build the `*-src/` tree with Maven (JDK 25) → copy
`target/quarkus-app/` into the matching `gateways/<x>/quarkus-app/` →
`docker compose build <svc> && up -d <svc>` (the compose build sections already pass the
`APPDIR: quarkus-app` build arg).

## Local patch list (check every item when upgrading)

### TM (jboss image / EAR)

1. **EAR patch** (`gazelle-tm-ear-12.1.0-patched.ear`):
   - `application.xml` gains `initialize-in-order=true` and the war module moved last
     (fixes the Seam/EJB startup deadlock made deterministic by fast PG17)
   - every `email/*.xhtml` template whitespace-trimmed (the indentation inside
     `<m:to>`-style elements is rejected by JavaMail 1.6's strict address parsing)
2. **javax.mail module 1.4.5 → 1.6.2** (Dockerfile): fixes Java 7 STARTTLS, which always
   failed against modern TLS servers
3. **entrypoint.sh** (baked into the image — changes require a rebuild):
   - renders the CAS properties and the EAP connector proxy attributes from `PUBLIC_*`
     on every boot
   - renders the mail session from `SMTP_*` (mail:1.1 schema: host/port/login/ssl/tls)
   - renames the mail-session JNDI binding to `java:jboss/mail` (what TM's
     components.xml actually looks up — not EAP's default `/Default`)
   - imports `mailpit/certs/ca.pem` into the JRE cacerts
   - adds `MAX_HEADER_SIZE=32768` to `JAVA_OPTS` (large-cookie 400s)
4. **`GAZELLE_LOGO.gif` in welcome-content**, home-page logo inlined as a data URI (the
   original external URL is dead)

### GUI (gui-src)

1. `src/shared/components/table/Table.tsx`: `display:block` on the virtualized tbody
   (Safari ignores positioning on table-internal boxes → lists overlapped the content
   below)
2. Maestro standalone support: env-controlled persist flag (`GZL_MAESTRO_PERSIST=false`)
   + inline report rendering in ValidationConfigurationPanel (no Datahouse)
3. Custom home page: `gui-home/` (`home.html` + `menu.yaml`, mounted via
   `GZL_HOME_CONFIGURATION_FOLDER`; templates support `${SCHEME}`/`${FQDN}`
   substitution)

### Keycloak

- Official dist moved onto Temurin JDK 25 (the provider is compiled for Java 25; the
  official image's JRE 21 throws UnsupportedClassVersionError)
- `realm-migration/` scripts patched for portability (`grep -oP` removed)
- `repeatable/smtp.json` wired to the `SMTP_STARTTLS`/`SMTP_SSL` env vars

### nginx (stack layer)

- `absolute_redirect off` (redirects leaked the internal port),
  `large_client_header_buffers 8 32k` (large cookies), forwarded-port inference maps
  (443/80 derived from proto when the proxy omits the port), resolver 127.0.0.11 +
  variable proxy_pass (nginx starts before its upstreams), and a regex location serving
  the RichFaces `plain/packed/packed.css` that upstream never shipped

### Database

- `authdb/00-md5-password.sh`: re-hashes the bootstrap password as md5 after initdb
  (the Java 7 driver cannot SCRAM)
- `authdb/init.sql`: creates the keycloak and gum databases
- db command: `-c password_encryption=md5 -c max_prepared_transactions=100`

## Version control

Git housekeeping done: junk removed, `.gitignore` in place (excludes `.env`, `backup/`,
`mailpit/certs/`, `downloads/`, `**/target/`, `**/node_modules/`), `.env.example`
provided. Large binaries (`downloads/` and the config-cli jar) are **not in git** —
they are distributed as a single Google Drive tarball (link in volume 02). When any of
those binaries changes, re-create and re-upload the tarball:

```bash
tar czf IHE-Gazelle-docker-binaries.tar.gz downloads/ keycloak/keycloak-config-cli-26.5.5.jar
```

On a fresh clone: download + extract the tarball, then regenerate `mailpit/certs/`
with the commands in volume 03 before the first start.
