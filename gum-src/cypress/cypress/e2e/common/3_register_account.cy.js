
describe('Test register account', () => {

  it('Redirect to TM for register account', () => {
    cy.go_to_gazelle_tool(`/gazelle`,"Gazelle")
    cy.go_to_login_page()

    cy.get('#register-link').click()

    // Check redirection to register page in TM
    cy.location().should((loc) => {
      expect(loc.href).to.eq(`https://${Cypress.env('FQDN')}/gum-ui`)
    })
  })

  it('Register to gazelle', () => {
      cy.go_to_gazelle_tool(`/gum-ui`,"Registration")

      cy.get('#username').type('cy_auth_new_user')
      cy.get('#password').type('password')
    })
})