# OIDC authentication

From GUM V4.0.0, it's possible to access to REST APIs with OIDC authentication.

## How it works

OpenID Connect (OIDC) is an identity authentication protocol that is an extension of open authorization (OAuth) 2.0 
to standardize the process for authenticating and authorizing users when they sign in to access digital services. 

This protocol is based on Json Web Tokens (JWT). These tokens are the proof of the authentication of the user.

In a JWT we can find some user information like : 
- the user id
- the user name
- the user email
- the user groups

## How to retrieve a token

It's possible to retrieve a token by using a curl command or a solution like Bruno.

### With CURL CLI

```bash
curl -X POST https://<hostName>/auth/realms/gazelle/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=OIDC_GAZELLE_CLIENT&grant_type=password&username=<email>&password=<password>'
```

This can give for example:

```bash
curl -X POST https://preprod.ihe-europe.net/auth/realms/gazelle/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=OIDC_GAZELLE_CLIENT&grant_type=password&username=vld@kereval.com&password=myPassword'
```

The response will be a JSON object with the token:

```json
{
  "access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJDVE5iMUtlQURQVmhfUDdDdkpXMGpPSE9CMXM4azFRbHpLcmt0RzZFSG5vIn0.eyJleHAiOjE3MjA2ODgzNDcsImlhdCI6MTcyMDY4ODA0NywianRpIjoiMDJmNmEzNjktNmYxMy00NzFlLTlmYTgtMTFhMjNkNjgxYWEyIiwiaXNzIjoiaHR0cHM6Ly9xdWFsaWYzLmloZS5rZXJldmFsLmNsb3VkL2F1dGgvcmVhbG1zL2dhemVsbGUiLCJhdWQiOiJodHRwczovL3F1YWxpZjMuaWhlLmtlcmV2YWwuY2xvdWQiLCJzdWIiOiJmOjI2MGMxYjlkLTIyYjEtNGIxNi1iOWJhLTAzMjVhOWIyMzRkNjowZTZhZTA0YS01YWVjLTQ3NDYtODY0NS0yY2M0MzQ1OGE3MjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJPSURDX0dBWkVMTEVfQ0xJRU5UIiwic2Vzc2lvbl9zdGF0ZSI6IjMxYjZhZTE4LTkwMGItNDA1Yy1iZTc4LWM0ZjdlM2NlZjI2NiIsInNjb3BlIjoicHJvZmlsZSBvaWRjLWNsaWVudC1zY29wZSBtaWNyb3Byb2ZpbGUtand0Iiwic2lkIjoiMzFiNmFlMTgtOTAwYi00MDVjLWJlNzgtYzRmN2UzY2VmMjY2IiwidXBuIjoiMGU2YWUwNGEtNWFlYy00NzQ2LTg2NDUtMmNjNDM0NThhNzI2Iiwib3JnYW5pemF0aW9uIjpbIi9LRVIyIl0sIm5hbWUiOiJWYWxlbnRpbiBMb3JhbmQiLCJncm91cHMiOlsib3JnOktFUjIiLCJhZG1pbl9yb2xlIiwib2ZmbGluZV9hY2Nlc3MiLCJhZG1pbiIsInVtYV9hdXRob3JpemF0aW9uIiwidXNlciJdLCJhdXRobl9tZXRob2QiOiJPSURDIiwiaWQiOiIwZTZhZTA0YS01YWVjLTQ3NDYtODY0NS0yY2M0MzQ1OGE3MjYiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiIwZTZhZTA0YS01YWVjLTQ3NDYtODY0NS0yY2M0MzQ1OGE3MjYiLCJnaXZlbl9uYW1lIjoiVmFsZW50aW4iLCJmYW1pbHlfbmFtZSI6IkxvcmFuZCJ9.Hq7qnjWf1JpUJMn1d1--SANH3AATKBnPlfMrTDJvbIygDLriNxpkcM8SYFYhb-I_NPfNsT0xn0XxfT_d_cD2-PFboK9zVY1SwoSnvfaBzOkmAPVkrYXFa3XEjD7UqHJ1dWgUkj7QiX8imokRTJ8TqGVEQWT60UtJPgjF08oOIfNE5F4SytSp0RNxDnq_Kk1SWoxW0nB0RLPc5bDNkv-hwrKfy8PRN6VSA_tbHfTnVgf7e2hFXW2CeAzORxNKRbm2IL7xXGaLquxXEv3aZnNQOE0o5rwpSX9BLcb1G92o3hczi6Y_ezFpyLwcyq_siO7hogX09IAuMBcOHwSxjV5rzw",
  "expires_in":120,
  "refresh_expires_in":1200,
  "refresh_token":"eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0MWE4NTgwNC0yZDM4LTRmMWEtYTZmNS03MWMwMThlZGE0MjIifQ.eyJleHAiOjE3MjA2ODkyNDcsImlhdCI6MTcyMDY4ODA0NywianRpIjoiMzA0ODhiMGEtMjAxZC00MTM4LWEzZjYtMjY5NzQzY2JmNGU0IiwiaXNzIjoiaHR0cHM6Ly9xdWFsaWYzLmloZS5rZXJldmFsLmNsb3VkL2F1dGgvcmVhbG1zL2dhemVsbGUiLCJhdWQiOiJodHRwczovL3F1YWxpZjMuaWhlLmtlcmV2YWwuY2xvdWQvYXV0aC9yZWFsbXMvZ2F6ZWxsZSIsInN1YiI6ImY6MjYwYzFiOWQtMjJiMS00YjE2LWI5YmEtMDMyNWE5YjIzNGQ2OjBlNmFlMDRhLTVhZWMtNDc0Ni04NjQ1LTJjYzQzNDU4YTcyNiIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJPSURDX0dBWkVMTEVfQ0xJRU5UIiwic2Vzc2lvbl9zdGF0ZSI6IjMxYjZhZTE4LTkwMGItNDA1Yy1iZTc4LWM0ZjdlM2NlZjI2NiIsInNjb3BlIjoicHJvZmlsZSBvaWRjLWNsaWVudC1zY29wZSBtaWNyb3Byb2ZpbGUtand0Iiwic2lkIjoiMzFiNmFlMTgtOTAwYi00MDVjLWJlNzgtYzRmN2UzY2VmMjY2In0.zpcBusnO7Z6cgONRoI0wXpBqGC73Lv70nBfb1f-gHBCn6aXksd2koqqp8uxU3BnsQrXZpXt0-Nw36gT8I0knxQ",
  "token_type":"Bearer",
  "not-before-policy":0,
  "session_state":"31b6ae18-900b-405c-be78-c4f7e3cef266",
  "scope":"profile oidc-client-scope microprofile-jwt"
}
```

Then, by extracting the `access_token` value, you can use it to access to the REST APIs.

Example of a curl command to access a REST API:

```bash
curl -X GET https://<hostName>/gum/rest/users -H 'Authorization: Bearer <access_token>'
```

### With Swagger-UI

When GUM is deployed, a swagger can be accessible at the following URL: `https://<hostName>/gum/swagger-ui/`.

This swagger allow us to execute some HTTP requests directly on GUM REST APIs.

For protected endpoints, you can log in with your email and password and retrieve a JWT. 

We use `implicit auth flow` in this case. The default lifespan of the token is 20 minutes. If you encounter 401 error responses,
tries to re-auth with a complete logout, it's probable that your token is expired.

Then, you can use this token to access to protected REST APIs.


### With Interfaces

When you are using the user interfaces, Authentication system works in the same way. 

It's just that there is an interface for the authentication to enter email + password but behind a JWT is retrieved and used for the REST API calls.

This JWT contains all the information about your profile. This is why when you change your name in user-interface, you must re-auth to update the name display in the interface, because you need a new JWT with updated information.

### Session management

JWTs has a limited lifespan. This is a security concern to avoid giving eternal access to an attacker that succeed to retrieve an access token.

The token provided by Keycloak has a default lifespan of **10 minutes**.

In case of trouble, it's possible to increase the lifespan of the delivered access token in the Gazelle Realm configuration page in Keycloak admin console.

Realm settings > Tokens > Access Tokens > Access Token Lifespan.

> :warning: **WARNING** Be careful if your access token is too long-lasting, you expose the system to more vulnerabilities.

In UI application, we set maxAge of the session to 10 minutes. The maxAge attribute is the limit of duration for an idle session (https://next-auth.js.org/configuration/options#session). 

If the user is active within 10 minutes, the maxAge will not delete the active session.

So, we implemented a silent re-auth in case of _401 Unauthorized_ responses. If the user is performing actions, the JWT will expire and in this case the back-end will not accept sent requests. As soon the jwt is detected expired with 401 response from backend, we trigger an attempt of authentication.

Thanks to this, Keycloak will directly detect the active session with user cookies and re-deliver a new token without giving user credentials. The page can be a bit slower to load in this case but it's an efficient way to refresh user token.