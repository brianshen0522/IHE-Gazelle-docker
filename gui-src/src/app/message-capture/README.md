# Proxy UI

This project is using Next.js, React, Typescript and TailwindCSS technologies.

## Configurations

### Install dependencies

```bash
npm install
```

### Environment variables

You need to create a `.env` file at the root of the project and fill it with the following variables:

| Variable name              | Description                                                           | Example                                              |
| -------------------------- | --------------------------------------------------------------------- | ---------------------------------------------------- |
| GZL_DTH_API_URL            | The URL of the Gazelle datahouse back-end service.                    | https://qualif2.ihe.kereval.cloud/datahouse/rest/v1  |
| GZL_DTH_EVSGATEWAY_URL     | The URL of EVS gateway to fetch validation profiles.                  | https://qualif2.ihe.kereval.cloud/evsgateway/rest/v1 |
| GZL_DTH_VALIDATION_ENABLED | The option to enable EVS validation                                   | true                                                 |
| HOSTNAME                   | It is possible to update the hostname of the server is running behind | localhost                                            |
| PORT                       | It is possible to update the port of the server is running behind     | 3000                                                 |

## Start application in dev mode

First, run the development server:

```bash
pnpm dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## Build & push docker image

```bash
docker build -t rg.fr-par.scw.cloud/gazelle-snapshot/app/gazelle-datahouse-ui:${APP_VERSION} .
```

```bash
docker push rg.fr-par.scw.cloud/gazelle-snapshot/app/gazelle-datahouse-ui:${APP_VERSION}
```

## Execute tests

### End to end tests

The end to end tests will be using [Cypress](https://www.cypress.io/).

You can open the Cypress test runner with the following command at the root of the project:

```bash
npx cypress open
```

[WARNING] The application must be started before running the tests.

### Unit tests

The unit tests utilize [React Testing Librairy](https://testing-library.com/) and [Jest](https://jestjs.io/).

They are located in the `__tests__` folder containing both the unit tests and their associated snapshots.

To execute the tests and view the test coverage report, run:

```bash
pnpm test
```

If changes occur and you need to update the snapshots, run:

```bash
pnpm test -- -u
```

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.
