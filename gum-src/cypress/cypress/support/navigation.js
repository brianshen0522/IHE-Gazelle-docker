// ***********************************************
// Commands linked to navigation
// ***********************************************

/**
 * Go to the external idp mock
 * @param {string} username - The text above username input text area
 * @param {string} password - The text above password input text area
 */
Cypress.Commands.add('go_to_idp_mock', (username, password,idpName) => {
    cy.get(`#social-${idpName}`).click();
    cy.get('body').contains(username)
    cy.get('body').contains(password)
})

/**
 * Go to the keycloak idp mock
 * @param {string} username - The text above username input text area
 * @param {string} password - The text above password input text area
 */
Cypress.Commands.add('go_to_keycloak_idp_mock', (username, password) => {
    cy.get(`#social-${Cypress.env('KEYCLOAK_IDP_NAME')}`).click();
    cy.get('body').contains(username)
    cy.get('body').contains(password)
})

/**
 * Go to the local login form
 */
Cypress.Commands.add('go_to_gazelle_login_form', () => {
    cy.get("#gazelle-login").click();
    cy.get('#username')
    cy.get('#password')
})

/**
 * Go to a specific gazelle tool
 * @param {string} path - The relative path of the tool
 * @param {string} h1 - The h1 of the tool
 */
Cypress.Commands.add('go_to_gazelle_tool', (path, h1) => {
    cy.visit(`http://${Cypress.env('GAZELLE_FQDN')}${path}/`)
    cy.get('body').contains(h1)
})

/**
 * Go to a specific gazelle tool
 * @param {string} path - The relative path of the tool
 */
Cypress.Commands.add('go_to_keycloak_mock_account', (path) => {
    cy.visit(`http://${Cypress.env('KEYCLOAK_FQDN')}${path}/`)
    cy.get('body').contains("Welcome to Keycloak account management")
});

/**
 * Go to the login page
 * @requires Be in a gazelle tool
 */
Cypress.Commands.add('go_to_login_page', () => {
    cy.location().then((location) => {
//            const encodedLocation = encodeURIComponent(location.href);
            // Button can be caught by id (id=loginButton) but not working with ipLoginButton
            cy.xpath('//*[contains(@id,"login")]').click()
//            cy.location().should((loc) => {
//                expect(loc.search).to.contains(`?service=${encodedLocation}`)
//                expect(loc.pathname).to.contains(`/realms/${Cypress.env('KEYCLOAK_REALM')}/protocol/cas/login`)
//            })
        });
});

/**
 * Go to the login page with an already logged session
 * @requires Be in a gazelle tool
 */
Cypress.Commands.add('go_to_login_page_already_logged', () => {
    cy.location().then((location) => {
        cy.xpath('//*[contains(@id,"login")]').click()
        cy.location().should((loc) => {
            expect(loc.href).to.contains(location.href)
        })
    })
})