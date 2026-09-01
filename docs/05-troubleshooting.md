# 05 — Troubleshooting

> Indexed by symptom. Every entry below was actually hit and its fix verified in this
> deployment.

## Login / SSO

| Symptom | Cause | Fix |
|---|---|---|
| Keycloak shows "invalid redirect_uri" after clicking Sign in | realm migration creates redirect URIs shaped `https://host*`; Keycloak wildcards only match at the **end of a path**, so that form never matches | `./scripts/apply-public-url.sh` (required on first install and after every URL change) |
| Keycloak says "Client not found" after TM login | TM generated an http:// CAS service URL (EAP ignores X-Forwarded-*) | verify the jboss container's `PUBLIC_*` env, then rebuild/recreate (the entrypoint rewrites the connector with proxy-name/proxy-port/secure) |
| Keycloak NPEs on every request (HostnameV2Provider) | public hostname contains an **underscore** | use a hyphenated hostname |
| GUI session shows organization: null after login | Keycloak's user-federation cache holds the pre-org-attach snapshot | `kcadm create clear-user-cache -r gazelle`, then log in again (bootstrap does this automatically) |
| Login spins and returns unauthenticated / NextAuth callback 502 | front proxy buffers too small for the large Set-Cookie | add `proxy_buffer_size 32k; proxy_buffers 8 32k; proxy_busy_buffers_size 64k;` on the proxy |
| **400 Request Header Or Cookie Too Large** | Keycloak + NextAuth cookies exceed 8k; three layers each have limits | this stack's nginx (`large_client_header_buffers 8 32k`) and TM (`MAX_HEADER_SIZE=32768`) are already fixed; **the external proxy needs the same** (defaults drop connections around 16k). Still failing → clear the domain's cookies |
| Which layer produced a 400? | — | `Transfer-Encoding: chunked` = the backend (JBoss) produced it, nginx proxied it; `Content-Length` = nginx's own (never trust the Server header — nginx rewrites it) |

## URLs / redirects

| Symptom | Cause | Fix |
|---|---|---|
| Visiting the root redirects to `http://…:8888/gazelle/home` | nginx's `/` 302 defaulted to an **absolute** Location (its own scheme/port) | fixed: `absolute_redirect off;` in nginx/default.conf |
| Token endpoint / issuer contains `:8888` | the front proxy sent `X-Forwarded-Port: 8888`, or this layer fell back to `$server_port` | proxy sends 443 or nothing (this layer infers 443/80 from proto); never send 8888 |
| Containers cannot reach the public URL | TLS proxy on the same machine; containers cannot hairpin to the public IP | put the proxy on a separate machine, or use local http mode |

## TM (JBoss)

| Symptom | Cause | Fix |
|---|---|---|
| Deployment fails: `IJ000453: Unable to get managed connection` | PG password stored as SCRAM; the Java 7 driver cannot SCRAM (initdb sets the bootstrap password before the md5 flag applies) | automated by `authdb/00-md5-password.sh`; manual: `ALTER USER gazelle WITH PASSWORD '<pw>';` then restart jboss |
| Deployment hangs forever in isdeploying (Seam/EJB deadlock) | PG17 is fast enough that Seam init races EJB start | fixed: EAR patch `initialize-in-order=true` + war module moved last (downloads/…-patched.ear) |
| TM receives an MQTT event but the DB write fails (JDBCConnectionException) | the db container was restarted; the EAP pool holds dead connections | `docker compose restart jboss`; mosquitto redelivers the missed organization event |
| Home page redirects to install/installation.seam | the TM DB has no organization | run bootstrap; if already run → see the previous row (MQTT write failed) |
| Creating a Test fails with `gazelle_language_id` NOT NULL | `default_gazelle_language` misconfigured | must be the language table's description value `EN` (not `English`) |
| TM mail fails: `No appropriate protocol` | JavaMail ≤1.5's STARTTLS enables every supported protocol (including policy-disabled SSLv3) — always fails against modern servers on Java 7 | fixed: the EAP javax.mail module was swapped to 1.6.2 (Dockerfile) |
| TM mail fails: `Local address contains control or whitespace` | JavaMail 1.6's strict address parsing vs. TM templates putting recipients in indented element text | fixed: all email/*.xhtml in the EAR were whitespace-trimmed |
| TM mail silently does nothing / session lookup fails | TM's Seam looks up `java:jboss/mail`, EAP binds `java:jboss/mail/Default` by default | fixed: the entrypoint renames the JNDI binding |
| `a.substr` console error | present on official Gazelle instances too (upstream) | harmless, ignore |
| RichFaces `plain/packed/packed.css` 404 | richfaces-components-ui 4.3.5 ships packed CSS for 9 skins but **not** `plain` (official instances 404 too) | fixed: the stack nginx answers that URL directly with the skin-independent packed.css content (regex location incl. the `;jsessionid` variant) |

## GUM / user management

| Symptom | Cause | Fix |
|---|---|---|
| TM boot logs "Failed to register CAS resources … invalid_grant" and CAS login / m2m tokens fail | race on a fresh DB with warm image caches: TM booted before keycloak-config-cli's repeatable import set the clients-admin password | fixed: the keycloak healthcheck now also requires the migration-done marker (`/tmp/realm-migration-done`), so dependents wait for the FULL migration; if hit on an older tree, `docker compose restart jboss` after keycloak settles |
| gum keeps restarting during a fresh install | its Flyway migration reads TM's `usr_users` table, which doesn't exist until TM deploys | normal; it self-heals once TM's schema exists |
| Every GUM API call returns 401 | the m2m token's `aud` doesn't match `M2M_AUDIENCE` | fresh DB: set it to PUBLIC_URL; pre-existing DB: keep the mapper's frozen value (volume 03) |
| GUM JWT env vars have no effect | the docs say `JWT_VERIFY_*`; the working names are `MP_JWT_VERIFY_*` | compose sets both |
| Tokens fetched via internal URLs are rejected | `KC_HOSTNAME_BACKCHANNEL_DYNAMIC=true` makes the issuer follow the request URL | always fetch tokens via `${PUBLIC_URL}/auth/...` (resolvable from inside containers too) |
| User/organization lists overlap the text below them in Safari | the virtualized table uses tbody relative + tr absolute; Safari ignores positioning on table-internal boxes | fixed: `display:block` on the tbody in gui-src Table.tsx (check whether upstream fixed it before upgrading the GUI) |

## Portals / validation chain

| Symptom | Cause | Fix |
|---|---|---|
| Portal lists go empty; registry logs Unauthorized every minute | registry/keycloak was restarted alone; services' WebSocket reconnect can't obtain a fresh token (upstream bug) | `docker compose restart simulation-gateway validation-gateway xml-validation-service maestro gum` |
| A service registers as (unknown, unknown) | `GZL_SERVICE_K8S_ID` missing or malformed (`name-instance-replica`) | set it, globally unique |
| GUM retries registry registration every minute (VIOLATED_POLICY - Unauthenticated / invalid_client_credentials) | GUM was missing `GZL_SERVICE_K8S_ID` **and** `GZL_M2M_CLIENT_SECRET` (its m2m lib registers the client with the env secret; unset = mismatched credentials); its service account may also lack the realm role `machine` | fixed in compose (`user-management-gum01-001` + `GUM_M2M_SECRET`); grant the role once with `kcadm add-roles -r gazelle --uid <service-account-id> --rolename machine` if still rejected |
| Validation hangs on "Validating" / Maestro step fails | the profile lacks `schematronVersion` (required even for XSD-only profiles) | add it to `xvs-resources/index.json`, restart xml-validation-service |
| The GUI reports page errors | it reads Datahouse, which is not deployed (private component) | known limitation; validation reports render inline instead |

## SMTP

| Symptom | Cause | Fix |
|---|---|---|
| SMTP settings changed in the Keycloak admin console revert | `repeatable/smtp.json` re-applies on every boot | edit the `SMTP_*` block in `.env` |
| GUM neither sends nor errors | `SMTP_MOCK_ENABLED=true` (log only) | set false, `up -d gum` |
| GUM STARTTLS config errors | Quarkus start-tls takes `REQUIRED/OPTIONAL/DISABLED`, not true/false | use `SMTP_STARTTLS_MODE` |
| TLS handshake fails against the self-signed server | a client doesn't trust the test CA | see the trust chain in volume 03 (KC_TRUSTSTORE_PATHS / keytool / TRUST_ALL) |

## Infrastructure

| Symptom | Cause | Fix |
|---|---|---|
| Edited a single-file bind mount but the container doesn't see it | macOS editing changes the inode; the old mount is dead | `docker compose up -d --force-recreate <service>`; reload is useless |
| Changed compose env but behavior didn't change | used `restart` (doesn't apply new env) | `docker compose up -d <service>` (recreate) |
| Keycloak XA errors (prepared transactions) | PG `max_prepared_transactions` too low | the db command already passes `-c max_prepared_transactions=100` |
| TM logs "organization already exists" after an MQTT restart | retained-message replay | harmless, ignore |
| Chrome cannot resolve `*.orb.local` | Chrome's async DNS ignores /etc/resolver | use `*.localhost` (Chrome maps it to 127.0.0.1) or real DNS |
