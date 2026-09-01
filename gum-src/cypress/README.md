# Cypress end to end testing

## Requirements

nodejs : v19.0.0
npm : v9.6.5

## Installation

Install cypress

```bash
npm install -D cypress@12.11.0

```

Install required modules

```bash
npm install cypress-xpath
npm install cypress-maildev
```

## Usage

Open the cypress window

```bash
cd gazelle-user-management
npx cypress open --project ./cypress
```

If you want to add external tests from another project like `ans-keycloak-resources` or `italian-keycloak-resources` you can add a **specPattern** parameter at your command like below :

```bash
cd gazelle-user-management
npx cypress open --project ./cypress --config specPattern=['**/cypress/e2e/**','../../ans-keycloak-resources/cypress/e2e/**','../../italian-keycloak-resources/cypress/e2e/**']
```

```bash
cd gazelle-user-management
npx cypress run --project ./cypress
```

## Architecture

The tests are located in the e2e/gum folder. There are splits in folder by feature. In each file, there are tests for each persona.

## Delete tests users

If you want to delete the users that ware added in keycloak by your tests you can execute the sql scripts in the script folders.
One is for the gum database and the other is for the keycloak database.
Don't forget to execute the keycloak database one because you may have a delegated user duplication that causes crashes at login.