
describe('Test logout Gazelle user', () => {

    it.skip('Logout feature in two applications', () => {
      cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
      cy.go_to_login_page()

      // Login with correct password
      cy.local_login(`${Cypress.env('GAZELLE_ADMIN_USERNAME')}`, `${Cypress.env('GAZELLE_ADMIN_PASSWORD')}`)
      cy.assert_is_logged_in_TM_as_admin();

      // Logout from Proxy
      cy.go_to_gazelle_tool(`/proxy`, "Proxy")
      cy.go_to_login_page_already_logged()
      cy.logout()
      cy.assert_is_not_logged();

      // Check not logged in TM
      cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
      cy.assert_is_not_logged();
    });
  })