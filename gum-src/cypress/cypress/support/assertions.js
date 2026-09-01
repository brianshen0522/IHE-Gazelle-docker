// ***********************************************
// Commands linked to navigation
// ***********************************************

import {translations} from './translations'
import '@testing-library/cypress/add-commands'

/**
 * Assert that the user is logged
 */
Cypress.Commands.add('base_assert_logged', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.location().then((location) => {
        expect(location.pathname).to.not.contains('/auth/realms')
    })
    cy.get('#navbar').contains(trad.uiLogin).should('not.exist')
})

/**
 * Assert that the user is not logged
 */
Cypress.Commands.add('base_assert_not_logged', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.location().then((location) => {
        expect(location.pathname).to.not.contains('/auth/realms')
    })
    cy.get('#navbar').contains(trad.uiLogin)
    cy.get('#navbar').contains(trad.uiLogout).should('not.exist')
})
Cypress.Commands.add('assert_is_on_missing_information_error_page',()=>{
    const trad = translations[Cypress.env('LOCALE')]

    cy.xpath(`(//*[contains(text(),kc-page-title)])`).contains(trad.sorry)
    cy.xpath(`(//*[contains(text(),kc-error-message)])`).contains(trad.firstName)
    cy.xpath(`(//*[contains(text(),kc-error-message)])`).contains(trad.lastName)
    cy.xpath(`(//*[contains(text(),kc-error-message)])`).contains(trad.organization)
})

Cypress.Commands.add('assert_is_on_organization_update_error',()=>{
    const trad = translations[Cypress.env('LOCALE')]

    cy.get('#kc-page-title').contains(trad.sorry)
    cy.get('#kc-content-wrapper').contains(trad.delegatedOrganizationUpdateFail)
    // cy.xpath(`(//*[contains(text(),kc-content-wrapper)])`).contains(trad.delegatedOrganizationUpdateFail)
    // cy.get('#kc-content-wrapper').find("alert-error pf-c-alert pf-m-inline pf-m-danger").find(".pf-c-alert__title .kc-feedback-text").contains(trad.delegatedOrganizationUpdateFail)
    // cy.get('#kc-content-wrapper').find('span').contains(trad.delegatedOrganizationUpdateFail)
    // cy.get('[class*=".pf-c-alert__title .kc-feedback-text"]')
    // cy.xpath(`(//*[contains(text(),kc-error-message)])`).contains(trad.delegatedOrganizationUpdateFail)


})

/**
 * Assert that the user is not logged in idp
 */
Cypress.Commands.add('assert_is_not_logged_idp', () => {
    cy.get('body').contains("Error")
    cy.get('body').contains("Invalid username or password")
})

/**
 * Assert that the request is invalid in idp
 */
Cypress.Commands.add('assert_request_invalid_idp', () => {
    cy.get('body').contains("Error")
    cy.get('body').contains("invalid_request")
    cy.get('body').contains("code challenge required")
})

/**
 * Assert that the user is still in login page so he is not logged in
 */
Cypress.Commands.add('assert_login_error', () => {
    cy.get('#username')
})

/**
 * Assert that the user is still in login page so he is not logged in
 */
Cypress.Commands.add('assert_local_login_in_delegated_orga_error', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.get('#kc-content-wrapper').contains(trad.pleaseLogWithProvider)

})

/**
 * Assert that the user is not logged
 */
Cypress.Commands.add('assert_is_not_logged', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.base_assert_not_logged()
    cy.get('#navbar').contains(trad.uiAdministration).should('not.exist')
})

/**
 * Assert that the user is logged in Gazelle app
 */
Cypress.Commands.add('assert_is_logged', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.base_assert_logged()
    cy.get('#navbar').contains(trad.uiAdministration).should('not.exist')
})

/**
 * Assert that the user is logged in TM
 */
Cypress.Commands.add('assert_is_logged_in_TM', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.base_assert_logged()
    cy.findByText(trad.uiAdministration).should('not.exist')
})


/**
 * Assert that the user is logged in Gazelle app as admin
 */
Cypress.Commands.add('assert_is_logged_as_admin', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.base_assert_logged()
    cy.get('#navbar').contains(trad.uiAdministration)
})

/**
 * Assert that the user is logged in TM as admin
 */
Cypress.Commands.add('assert_is_logged_in_TM_as_admin', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.base_assert_logged()
    cy.findByText(trad.uiAdministration).click()
    cy.findByText(trad.uiConfigureApplication).should('exist')
})

/**
 * Assert the login failed because of bad credentials
 */
Cypress.Commands.add('assert_login_failed_bad_credentials', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.get('#input-error').contains(trad.uiFailedLoginBadCredentials)
})

/**
 * Assert that the login failed because of blocked account
 */
Cypress.Commands.add('assert_login_failed_blocked_account', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.get(".pf-c-alert__title").contains(trad.uiFailedLoginDisabledAccount)
})

/**
 * Assert that the user is logged in keycloak
 */
Cypress.Commands.add('assert_login_keycloak', () => {
    cy.get("#landingSignOutButton").should('be.visible')
})

/**
 * Assert that the user is logged out keycloak
 */
Cypress.Commands.add('assert_logout_keycloak', () => {
    cy.get("#landingSignInButton").should('be.visible')
})

Cypress.Commands.add('assert_failed_login_delegated', () => {
    const trad = translations[Cypress.env('LOCALE')]
    cy.get('#kc-content-wrapper').contains(trad.uiFailedLoginDelegatedAccount)
})