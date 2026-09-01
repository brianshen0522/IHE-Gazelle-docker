import {translations} from '../support/translations';

describe('Test reset password', () => {
  const futurePassword = "test"
  const trad = translations[Cypress.env('LOCALE')]

  it.skip('Ask for reset password email', () => {
    // Go to TM
    cy.go_to_gazelle_tool(`/gazelle`,"Gazelle")
    cy.go_to_login_page()

    // Check presence of reset password link on keycloak page and click on it
    cy.get('#forgot-password-link').click()

    // Enter username and click on reset password button
    cy.get('#username').type(`${Cypress.env('GAZELLE_ADMIN_EMAIL')}`)
    cy.get('.pf-c-button').click()

    // Wait 1 second (TODO: find a better way to wait for the email to be sent)
    cy.wait(1000)

    // Check mail for reset password
    cy.maildevGetLastMessage().then((email) => {
      expect(email.subject).to.contains(trad.emailSubjectResetPassword);
      const urlToResetPassword = email.text.match(/https(.*)\n/)[0]
      cy.visit(urlToResetPassword)

      cy.maildevDeleteMessageById(email.id)
    });
    // Enter new password and confirm it
    cy.get('#password-new').type(futurePassword)
    cy.get('#password-confirm').type(futurePassword)
    cy.get('.pf-c-button').click()

    // Check redirection to Gazelle TM with logged session
    cy.location().should((loc) => {
      expect(loc.href).to.contains(`https://${Cypress.env('FQDN')}/gazelle/home.seam`)
    })
    cy.assert_is_logged_as_admin();

    // cy.get("#kc-page-title").contains("Votre compte a été mis à jour")

    // Wait 1 second (TODO: find a better way to wait for the email to be sent)
    cy.wait(1000)

    // Check mail for updated password
    cy.maildevGetLastMessage().then((email) => {
      expect(email.subject).to.contains(trad.emailSubjectPasswordUpdated);
      expect(email.text).to.contains(trad.emailBodyPasswordUpdated);

      cy.maildevDeleteMessageById(email.id)
    });
  });


  it.skip('Login with new password', () => {
        // Go to Proxy
        cy.go_to_gazelle_tool(`/proxy`,"Proxy")
        cy.go_to_login_page()

        // Login with correct password
        cy.local_login(`${Cypress.env('GAZELLE_ADMIN_EMAIL')}`, futurePassword);
        cy.assert_is_logged_as_admin()
  });
})