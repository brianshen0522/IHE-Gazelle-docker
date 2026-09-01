# Front architecture

Gazelle user interface is a mono application that contains several modules. Each module is a front of a Gazelle microservice. Each module must be independent, possible to have some shared components but in a limited way.

Two NextJS applications :

- Gazelle user interface (https://gitlab.inria.fr/gazelle/private/kereval/gazelle-user-interface)
- Gazelle admin interface (https://gitlab.inria.fr/gazelle/private/kereval/gazelle-admin-interface)

Shared NO BUSINESS widgets UI + hooks (ex :isSmallScreen) + assets between the two applications -> Gazelle Component UI
Shared BUSINESS components/hooks with logic between the two applications -> ???

Base Path :
`/gazelle` for Gazelle user interface
`/gazelle/admin` for Gazelle admin interface

## Package manager

Privilege pnpm to start in dev mode 

```bash
pnpm dev
```

build

```bash
pnpm build
```


## Structure

```bash
gazelle-user-interface/
|- src/
    |- app/
        |- layout.tsx
        |- error.tsx [DEFAULT]
        |- not-found.tsx
        |-- message-capture/
            |- components/
              |- list-messages/
                  |- hooks/
                    |- swr/
                  |- actions/
                  |- assets/
                |- ListMessages.tsx
                |- ListMessages.test.tsx
              |- message-details/
                  [...]
            |- shared-components/ [CAN BE SHARED]
              [...]
            |- assets/
           |- page.tsx [SERVER SIDE]
           |- layout.tsx
           |- error.tsx
           |- api/
        |-- simulation-portal/
            [...]
        |-- test-execution/
            [...]
        |-- user-management/
            [...]
        |-- home/
            [...]
        |-- api/
          |- auth/
              |- route.tsx
          |- env/ [TO REPLACE BY SERVER ACTIONS]
              |- route.tsx
    |- public
    |- config
    |- Dockerfile
    |- .env
    |- next.config.js
    |- tsconfig.json
    |- tailwind.config.js
    |- package.json
    |- package-lock.json
```