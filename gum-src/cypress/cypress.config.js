const { defineConfig } = require("cypress");
const { Client } = require('pg');

module.exports = defineConfig({
  e2e: {
//    specPattern: ['**/cypress/e2e/**','../../ans-keycloak-resources/cypress/e2e/**'],
    setupNodeEvents(on, config) {
      on('before:browser:launch', (browser, launchOptions) => {
        if (browser.name === 'edge' || browser.name === 'chrome') {
          // TODO configure browser locale
          // launchOptions.preferences.default.intl = { accept_languages: 'en-EN' };
          // launchOptions.args.push('--lang=en');
          // launchOptions.preferences.default.intl = { accept_languages: 'fr-FR' };
          return launchOptions;
        }
      });
         on("task", {
           async connectGUMDBWithParameters({sqlQuery, parameter}){
            return await sendSQLRequest('localhost', 'gazelle', 'gazelle', 5432, 'gum', sqlQuery, parameter);
           }
         });
         on("task", {
           async connectKeycloakDBWithParameters({sqlQuery, parameter}){
            return await sendSQLRequest('localhost', 'gazelle', 'gazelle', 5432, 'keycloak', sqlQuery, parameter);
           }
         });
        on("task", {
            async connectGazelleDBWithParameters({sqlQuery, parameter}){
                return await sendSQLRequest('localhost', 'gazelle', 'gazelle', 5432, 'gazelle', sqlQuery, parameter);
            }
        });
    },
  },
  "reporter": "junit",
  "reporterOptions": {
    "mochaFile": 'output.xml',
  }
});

async function sendSQLRequest(host, username, password, port, database, sqlQuery, parameter){
  const client = new Client({
     user: username,
     password: password,
     host: host,
     database: database,
     ssl: false,
     port: 5432
  });

  await client.connect();
  if (parameter) {
    let arr = sqlQuery.split("\n");
    for (let i = 0; i < sqlQuery.length; i++) {
        if (arr[i]) {
            await client.query(arr[i], [parameter])
        }
    }
  } else {
    await client.query(sqlQuery)
  }
  await client.end()
  return "ok";
}
