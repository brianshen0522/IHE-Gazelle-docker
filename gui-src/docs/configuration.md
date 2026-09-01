# Configuration

Gazelle User Interface (GUI) can be configured depending on the context where the application is deployed.

## Global environment variables

To work properly, application needs some environment variables at runtime.

Here you can find the list of the variables.

| Variable name        | Description                 | Example                     |
|----------------------|-----------------------------|-----------------------------|
| SCHEME               | Scheme for http access      | https                       |
| FQDN                 | Full Domain Qualified Name  | gazelle.ihe.net             |
| NEXT_PUBLIC_BASE_URL | Base url of the application | ${SCHEME}://${FQDN}/gazelle |

## Authentication

Gazelle User Interface (GUI) is using Keycloak for authentication. The application is using the `next-auth` library to manage the authentication.

To work properly, application needs some environment variables listed below.

| Variable name      | Description                                       | Example                                      |
|--------------------|---------------------------------------------------|----------------------------------------------|
| KEYCLOAK_ISSUER    | The issuer of JWT                                 | https://gazelle.ihe.net/auth/realms/gazelle  |
| KEYCLOAK_CLIENT_ID | The client ID of Keycloak used for authentication | OIDC_GAZELLE_CLIENT                          |
| NEXTAUTH_URL       | The url of next-auth api                          | https://gazelle.ihe.net/api/auth             |
| NEXTAUTH_SECRET    | A secret used by next-auth                        | secret                                       |
| GZL_GUM_API_URL    | The url of GUM backend REST api                   | https://gazelle.ihe.net/gum/rest             | 

## Excluded path

Thanks to `proxy.ts` you can exclude some paths are runtime. Give a list of paths to exclude in the `EXCLUDED_PATHS` variable. 

For example, if you want to exclude `/test-execution` and `simulation-portal` paths, you can do it like this:

```.dotenv
GZL_EXCLUDED_PATHS="/test-execution,/simulation-portal"
```

## Modules configuration

Be careful each module has its own configurations so pay attention to the module you need to configure.

### [Home](./home/configuration.md)

### [Message capture](./message-capture/configuration.md)

### [User management](./user-management/configuration.md)

### [Test execution](./test-execution/configuration.md)

### [Simulation portal](./simulation-portal/configuration.md)

