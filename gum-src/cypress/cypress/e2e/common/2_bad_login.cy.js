import { translations } from '../../support/translations';

describe('Test bad local login', () => {
        const trad = translations[Cypress.env('LOCALE')]

        before(() => {
          cy.readFile('data/remove_user.sql').then((sqlQuery) => {
            cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'cy_auth_inactive_vendor_admin' });
            cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'cy_auth_blocked' });
          })
          cy.readFile('data/before_bad_login.sql').then((sqlQuery) => {
                cy.task("connectGUMDBWithParameters",{sqlQuery: sqlQuery, parameter: '' }).then(cy.log);
          });
        });

        after(() => {
          cy.readFile('data/remove_user.sql').then((sqlQuery) => {
            cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'cy_auth_inactive_vendor_admin' });
            cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'cy_auth_blocked' });
          })
        });

        // Test 1
        it('Authentication blocked user', () => {
            // Go to login page
            cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
            cy.go_to_login_page()

            // Login with bad password
            cy.local_login('cy_auth_blocked', "BadPassword")
            cy.assert_login_failed_bad_credentials()

            // Login with correct password
            cy.local_login('cy_auth_blocked', "password")
            cy.assert_login_failed_blocked_account()
        })

        // Test 2
        it('Authentication inactive vendor admin', () => {
            // Go to login page
            cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
            cy.go_to_login_page()

            // Login with bad password
            cy.local_login('cy_auth_inactive_vendor_admin@gazelle.com', "BadPassword")
            cy.assert_login_failed_bad_credentials()

            // Login with correct password
            cy.local_login('cy_auth_inactive_vendor_admin@gazelle.com', "password")
            cy.assert_login_failed_blocked_account()

            // Check mail for inactive user
            cy.mhDeleteAll();
            cy.mhGetMailsByRecipient("cy_auth_inactive_vendor_admin@gazelle.com").mhFilterBySubject(trad.emailSubjectInactiveUser).should('have.length', 1);
            cy.mhDeleteAll();
        })

        // Test 3
        it('Authentication bad password', () => {
            cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
            cy.go_to_login_page()

            cy.local_login('cy_auth_user', 'badPassword')
            cy.get('#input-error').should('exist')
        });

        // Test 4
        it('Authentication bad username', () => {
            cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
            cy.go_to_login_page()

            cy.local_login('non_existing_user', 'password')
            cy.get('#input-error').should('exist')
        });
    })