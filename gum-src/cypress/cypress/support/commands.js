// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
import {translations} from './translations'
import 'cypress-mailhog';

Cypress.Commands.add('reset_password', (username) => {
    // Click on reset password link
    cy.get('a[href*="/reset-credentials"]').click()
    cy.get('#username').type(username)
    cy.get('.pf-c-button').click()
})

/**
 * Login to the application through keycloak
 * @param {string} username_input - Id of the Username input
 * @param {string} password_input - Id of the password input
 * @param {string} login_button - Text in the login button
 * @requires Be in the idp login page
 */
Cypress.Commands.add('login_idp', (usernameInput, username, passwordInput, password, buttonId, login_button) => {
    cy.get(usernameInput).clear().type(username)
    cy.get(passwordInput).type(password)
    cy.get(buttonId).contains(login_button).click();
})

Cypress.Commands.add('login_idp_missing_information', (username_input, password_input, login_button) => {
    cy.get(username_input).clear().type('apresso')
    cy.get(password_input).type('pwd')
    cy.get('button').contains(login_button).click();
})


/**
 * Login to the application through keycloak
 * @requires Be redirected by the idp to the terms and condition page
 */
Cypress.Commands.add('accept_consent_if_present', () => {
    cy.url().then(($url) => {
        if($url.includes('GAZELLE_TERMS_OF_SERVICE')) {
            cy.get('#kc-accept').click()
        } else  {
            cy.log('Already accepted terms and conditions')
        }
    })
})

/**
 * Login locally to the application through keycloak
 * @param {string} email - The email of the user
 * @param {string} password - The password of the user
 * @requires Be in the login page
 */
Cypress.Commands.add('local_login', (email, password) => {
    cy.origin("http://localhost:28080", { args: {email,password}},  ({email,password}) => {
        cy.get("body").then(($body) => {
            if ($body.find('#gazelle-login').length > 0 && $body.find('#gazelle-login').is(':visible')) {
                cy.get('#gazelle-login').click()
            }
        });

        cy.get('#kc-page-title')
        cy.get('#username').clear().type(email)
        cy.get('#password').type(password)
        cy.get('#kc-login').click()
    });


})

/**
 * Give consent to the application
 */
Cypress.Commands.add('give_consent', () => {
    cy.get('#kc-accept').click()
})

/**
 * Logout from the application
 * @param {string} check_login - The text to check if the user is logged in
 * @param {string} logout_button - The id of the logout button
 */
Cypress.Commands.add('logout', (check_login, logout_button) => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.get('#navbar').contains(check_login)
    cy.get(logout_button).click()
    cy.xpath(`(//*[contains(text(),"${trad.uiLogout}")])[last()]`).click()
    //cy.get('#j_idt221 > a').click()
    cy.xpath('//*[contains(@id,"login")]')
})

/**
 * Logout from the application
 */
Cypress.Commands.add('logout', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.get(".navbar-right > .dropdown > .dropdown-toggle").last().click()
    cy.xpath(`(//*[contains(text(),"${trad.uiLogout}")])[last()]`).click()
    // cy.xpath('//*[contains(@id,"login")]')
})

/**
 * Logout from the application
 */
Cypress.Commands.add('logout_from_idp_mock', () => {
    cy.visit(`http://${Cypress.env('IDP_MOCK_FQDN')}/Account/Logout`)
    cy.get('[class*="btn btn-primary"]').first().click()
})

Cypress.Commands.add('go_to_preferences', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.get(".navbar-right > .dropdown > .dropdown-toggle").first().click()
    cy.xpath(`(//*[contains(text(),"${trad.preferences}")])[last()]`).click()
})

/**
 * Update the SMTP configuration in keycloak
 * @param {string} host - The host of the SMTP server
 * @param {string} port - The port of the SMTP server
 * @param {string} username - The username of the SMTP server
 * @param {string} password - The password of the SMTP server
 */
Cypress.Commands.add('UpdateSMTPConfig', (host, port) => {
    cy.visit(`http://${Cypress.env('KEYCLOAK_FQDN')}/admin/master/console/#/gazelle/realm-settings/email`)
    cy.get('#username').clear().type(`${Cypress.env('KC_BOOTSTRAP_ADMIN_USERNAME')}`)
    cy.get('#password').type(`${Cypress.env('KC_BOOTSTRAP_ADMIN_PASSWORD')}`)
    cy.get('#kc-login').click()
    cy.get('.pf-c-form__group-control > .pf-c-switch > .pf-c-switch__toggle',{ timeout: 10000 }).click().click();
    cy.get("#kc-host").clear().type(`${host}`);
    cy.get("#kc-port").clear().type(`${port}`);

    cy.get('[data-testid="email-tab-save"]').click();
})

Cypress.Commands.add('full_login',(username, password) => {
    // Go to TM login page
    cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
    cy.go_to_login_page()

    // Go to IDP mock
    cy.go_to_idp_mock("Username", "Password", `${Cypress.env('IDP_NAME')}`)

    // Login to IDP mock
    cy.login_idp(`#${Cypress.env('IDP_USERNAME_INPUT')}`, username, `#${Cypress.env('IDP_PASSWORD_INPUT')}`, password, "button", `${Cypress.env('IDP_LOGIN_BUTTON')}`)
    cy.accept_consent_if_present()
})

/**
 * Unblock an user
 * @param {string} username - The username of the user
 */
Cypress.Commands.add('UnblockUserKeycloak', (username) => {
    cy.visit(`https://${Cypress.env('FQDN')}/auth/admin/master/console/#/gazelle/users`)
    cy.get('#username').clear().type(`${Cypress.env('KC_BOOTSTRAP_ADMIN_USERNAME')}`)
    cy.get('#password').type(`${Cypress.env('KC_BOOTSTRAP_ADMIN_PASSWORD')}`)
    cy.get('#kc-login').click()

    cy.get(".pf-c-form-control",{ timeout: 10000 }).clear().type(username);
    cy.get('.pf-c-input-group > .pf-c-button').click();

    cy.contains('a', username).click();
    cy.get('#temporaryLocked').then(($locked) => {
        if (!$locked.hasClass('disabled')) {
          $locked.click()
        }
    })
})