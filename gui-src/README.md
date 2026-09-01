# Gazelle User Interface

This project is using Next.js v.15, React, TypeScript, and TailwindCSS technologies.

<!-- TOC -->
* [Gazelle User Interface](#gazelle-user-interface)
* [🏗️ Global Architecture](#-global-architecture)
    * [Structure Overview](#structure-overview)
    * [Focus on a basic application structure](#focus-on-a-basic-application-structure)
      * [How it works](#how-it-works)
  * [Configurations](#configurations)
    * [Install dependencies](#install-dependencies)
    * [Environment variables](#environment-variables)
  * [Start application in dev mode](#start-application-in-dev-mode)
  * [Build docker image](#build-docker-image)
  * [Start application](#start-application)
  * [Executing the tests](#executing-the-tests)
    * [Run Tests](#run-tests)
    * [Update Snapshots](#update-snapshots)
  * [Internationalization](#internationalization)
  * [Learn More](#learn-more)
  * [License](#license)
<!-- TOC -->


# 🏗️ Global Architecture

The Gazelle User Interface is organized as a monorepository. Here is a detailed breakdown of the main structure:

### Structure Overview

Below is a diagram and detailed explanation of the global architecture:

```text
/gazelle-user-interface
│
├── src/                            # Main source code
│   ├── app/                        # User interface applications entrypoint and routing
│       ├── assets/                 # Static assets (images, fonts, etc.)
│       ├── app-1/                  # Application 1
│       ├── app-2/                  # Application 2
│
|── i18n/                           # Internationalization resources
│   ├── i18n                        # i18n setup and helpers
│   └── languages                   # Supported language files with assets (flags, ...)
|
|── shared/                         # Shared code between apps (utilities, components, hooks)
│   ├── api/                        # Shared Nextjs API routes between apps
│   ├── actions/                    # Shared actions between apps
│   ├── assets/                     # Shared static assets
│   ├── auth/                       # Shared authentication logic
|   ├── components/                 # Shared React components that cannot be in UI library
|   ├── config/                     # Shared configuration files
│     ├── navbarConfigGenerator     # Navigation bar configuration
│     └── rewrites                  # URL rewrite rules for multi-zones configuration between admin-interface and user-interface
|   ├── context/                    # Shared React context
|   ├── hooks/                      # Shared React hooks
|   ├── services/                   # Shared service functions (API calls, helpers)
|   ├── types/                      # Shared types
|   ├── utils/                      # Shared utility functions that are generic and reusable
|
|── proxy.ts                        # Custom proxy for authentication and protected routes
|
├── package.json                    # Root package: scripts & dependencies for the whole monorepository
└── ...                             # (Other root-level config files)
```

### Focus on a basic application structure

```text
 /gazelle-user-interface
 │
 ├── app-1/                         # Application 1
 │   ├── api/                       # API route handlers for app-1
 │   ├── item/                      # Item detail page (Server Side Rendering -> SSR*)
 │   ├── create/                    # Item creation page (SSR)
 │   ├── context/                   # React context providers for app-1
 |
 │   ├── components/                # UI components specific to app-1 (below typical e.g. for item/)
 │      ├── auth/                   # Authentication-related UI components
 │      ├── item/                   # Components specific to item
 │          ├── actions.ts          # Specific server actions for item
 │          ├── Types.ts            # Specific TypeScript types and interfaces for item
 │          ├── Child components    # Client-side React components used within item (forms, UI logic)
 │          ├── Unit tests          # Specific unit tests for item
 │      ├── ui/                     # Specific UI components (it is recommended to use UI library whenever possible)
 |
 │   ├── hooks/                     # Custom React hooks for app-1 logic
 │   ├── services/                  # Service functions (API calls, helpers)
 │   ├── utils/                     # Utility functions that are generic and reusable
 │   ├── types/                     # TypeScript types and interfaces
 │   ├── unauthorized/              # Unauthorized access handling
 │   ├── actions.ts                 # Server actions for app-1
 │   ├── layout.tsx                 # Layout component for app-1 pages
 │   ├── page.tsx                   # Main entry page for app-1
```

#### How it works

- **Modular source structure:**

    - The `src/` directory contains the main application code, with subfolders for each major app (e.g., `app-1/`).
    - This modular approach allows for easy expansion and separation of concerns.

- **Centralized configuration:**

    - The `config/` directory holds shared configuration files, such as navigation and URL rewrites, used across the
      admin interface and link to the user interface.

- **Internationalization (i18n):**

    - The `i18n/` directory manages translation resources, language files, and i18n helpers for multi-language support.

- **Shared codebase:**

    - The `shared/` directory provides reusable assets, authentication logic, React components, and hooks that cannot be
      added to the Gazelle component library or yet to be added. The main goal is code reuse and consistency across
      features.

- **Custom proxy:**

    - The `proxy.ts` file contains logic for authentication and route protection, ensuring secure access to the admin
      interface.

- **Monorepo tooling:**
    - `.pnpm-workspace.yaml` enables pnpm to manage dependencies efficiently across all apps and packages.
    - `turbo.json` (if used) leverages Turborepo for fast, cache-enabled builds and orchestrated tasks.

## Configurations

### Install dependencies

To install all dependencies, run:

```bash
npm install
# or
pnpm install
```

### Environment variables

You need to create a `.env` file at the root of the project and fill it with the following variables:

| Variable name                          | Description                                                              | Example                                                              |
|----------------------------------------|--------------------------------------------------------------------------|----------------------------------------------------------------------|
| BASE_URL                               | Base url of Next                                                         | http://localhost/                                                    |
| KEYCLOAK_ISSUER                        | Url of the Keycloak instance used for                                    | http://localhost/auth/realms/my-realm                                |
| KEYCLOAK_CLIENT_ID                     | Name of the Keycloak client used for authentication                      | MY_CLIENT                                                            |
| NEXTAUTH_URL                           | Url used by NextAuth for authentication                                  | http://localhost/gazelle/api/auth/                                   |
| NEXTAUTH_SECRET                        | Secret used by NextAuth for authentication                               | secret                                                               |
| GZL_GUM_API_URL                        | Gazelle User Management backend API url                                  | http://localhost/gum/rest                                            |
| GZL_DTH_API_URL                        | Datahouse backend API url                                                | http://localhost/datahouse/rest/v1                                   |
| GZL_DTH_EVSGATEWAY_URL                 | EVS Gateway backend API url                                              | http://localhost/evsgateway/rest/v1                                  |
| GZL_TM_URL                             | Test Management url                                                      | http://localhost/tm/                                                 |
| GZL_REGISTRATION_URL                   | User registration URL                                                    | http://localhost/gazelle/user-management/registration                |
| GZL_TEST_CASE_FOLDER                   | A folder containin test cases to execute                                 | /opt/gazelle-user-interface/test-execution/test-case/                |
| GZL_TEST_EXECUTION_URL                 | Test Execution URL                                                       | http://localhost/test-execution/rest                                 |
| GZL_MAESTRO_URL                        | Maestro URL                                                              | http://localhost:8150/maestro                                        |
| GZL_SIMULATION_GATEWAY_URL             | Simulation Gateway URL                                                   | http://localhost/simulation-gateway/rest/simulation/v1/sequences     |
| GZL_SIMULATION_REQUEST_TIMEOUT_SECONDS | Maximum duration in seconds that the simulator will wait for SUT message | 60                                                                   |
| GZL_DTH_VALIDATION_ENABLED             | Enable Datahouse validation                                              | true                                                                 |
| GDH_PRESENTATION_SCHEMA_PATH           | Path to the presentation schema (JSON file) used by Datahouse            | /opt/gazelle-user-interface/message-capture/presentationSchemas.json |
| GZL_HOME_CONFIGURATION_FOLDER          | Path to the JSON file used for the home page configuration               | /opt/gazelle-user-interface/home                                     |

## Start application in dev mode

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
```

Open [http://localhost:3000/gum-ui](http://localhost:3000/gum-ui) with your browser to see the result.

## Build docker image

```bash
docker build -t rg.fr-par.scw.cloud/gazelle-snapshot/app/gazelle-user-interface:${APP_VERSION} .
```

## Start application

```bash
docker-compose up -d
```

## Executing the tests

This project uses [React Testing Library](https://testing-library.com/), [Jest](https://jestjs.io/) and [**Vitest
**](https://vitest.dev/) for unit testing.

### Run Tests

To execute unit tests:

```bash
pnpm test
```

To run tests with coverage:

```bash
pnpm test:coverage
```

To start the Vitest UI with coverage reports:

```bash
pnpm vitest:coverage
```

### Update Snapshots

If you make changes that affect test snapshots, update them with:

```bash
pnpm test -- -u
```

## Internationalization

We use Crowdin to manage our translations for internationalization.

To use crowdin CLI, you must install the package in local using the command :

```bash
sudo npm i @crowdin/cli -g
```

If you need to add some translations, Add them manually to the english translations
file `public/locales/en/translation.json`.

> [!warning] :warning: Warning
> To use the Crowdin plugin, you need an API token.

To create a Crowdin API token, go in the settings of your account (top right of the page when you are logged), then API
and in **Personal Access Tokens** click on _New Token_.
You will be redirected to a page where you need to select scopes. To be able to update the translations of this project,
your token must have **at least** the following scopes:

- Projects (List, Get, Create, Edit) -> Read-only
- Source files & strings -> Read and Write
- Translations -> Read and Write

Once your token is created, make sure to save it somewhere secured as you will not be able to see it again. To use it you
need to export it like this:

```bash
CROWDIN_API_TOKEN=<my-token-value>
```

Then, you can upload this translation file to Crowdin with the following command :

```bash
npm run upload-translations
# or
pnpm run upload-translations
```

Then, translate the elements directly from [Crowdin editor interface](https://crowdin.com/editor/gazelle-ui).

When it's done, you can retrieve all the translations by executing the following command :

```bash
npm run download-translations
# or
pnpm run download-translations
```

This command will build the Crowdin project and then download JSON translation files and place them in the
folder `locales/<locale>/gzl_user_interface_translation.json`.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) – learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) – an interactive Next.js tutorial.


## License

    ```
    Copyright 2024-2026 IHE International
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    ```
