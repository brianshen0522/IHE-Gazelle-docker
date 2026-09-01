import {translations} from '../support/translations'

describe('Test block account', () => {
  const trad = translations[Cypress.env('LOCALE')]

  it.skip('Brute force user', () => {
    // Go to TM
    cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
    cy.go_to_login_page()

    // Login with correct password
    cy.local_login(`${Cypress.env('GAZELLE_USER_USERNAME')}`, `${Cypress.env('GAZELLE_USER_PASSWORD')}`)
    cy.assert_is_logged_in_TM()
    cy.logout()

    cy.go_to_login_page()
    for (let i = 0; i < 5; i++) {
      // Login with bad password
      cy.local_login(`${Cypress.env('GAZELLE_USER_USERNAME')}`, "BadPassword")
      cy.assert_login_failed_bad_credentials()
      cy.wait(1000)
    }
  });


  it.skip('Login to temporary locked user', () => {
    //Visite TM
    cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
    cy.go_to_login_page()

    // Login with correct password
    cy.local_login(`${Cypress.env('GAZELLE_USER_USERNAME')}`, `${Cypress.env('GAZELLE_USER_PASSWORD')}`)
    cy.assert_login_failed_bad_credentials()
    cy.wait(1000)

    // Retrieve the last sent email
    cy.maildevGetLastMessage().then((email) => {
      expect(email.subject).to.contains(trad.emailSubjectBlockedAccount);
      expect(email.text).to.contains(trad.emailBodyBlockedAccount);
      expect(email.to[0].address).to.equal(`${Cypress.env('GAZELLE_USER_EMAIL')}`);

      cy.maildevDeleteMessageById(email.id)
    });
  });

  after(() => {
    cy.UnblockUserKeycloak(Cypress.env('GAZELLE_USER_USERNAME'))
  });
})