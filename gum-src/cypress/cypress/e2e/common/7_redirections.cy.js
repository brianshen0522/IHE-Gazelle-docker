
describe('Test redirections', () => {
  
  it.skip('Redirection in TM', () => {
    const urlToBeRedirected = `https://${Cypress.env('FQDN')}/gazelle/users/user/listUsersInstitution.seam?`
    
    // Browse url requires to be logged in
    cy.visit(urlToBeRedirected)

    // Check redirection to login page
    cy.location().should((loc) => {
      expect(loc.pathname).to.contains('/auth/realms')
    })

    // Login with correct password
    cy.local_login(`${Cypress.env('GAZELLE_ADMIN_USERNAME')}`, `${Cypress.env('GAZELLE_ADMIN_PASSWORD')}`)

    // Check redirection to initial url
    cy.location().should((loc) => {
      expect(loc.href).to.equals(urlToBeRedirected)
    })
  })
  

  it.skip('Redirection in Proxy', () => {
    const urlToBeRedirected = `https://${Cypress.env('FQDN')}/proxy/admin/configure.seam`

      // Browse url requires to be logged in
      cy.visit(urlToBeRedirected)

      // Check redirection to login page
      cy.location().should((loc) => {
        expect(loc.pathname).to.contains('/auth/realms')
      })
  
      // Login with correct password
      cy.local_login(`${Cypress.env('GAZELLE_ADMIN_USERNAME')}`, `${Cypress.env('GAZELLE_ADMIN_PASSWORD')}`)
  
      // Check redirection to initial url
      cy.location().should((loc) => {
        expect(loc.href).to.equals(urlToBeRedirected)
      })
    })


    it.skip('Redirection in TM with parameters', () => {
      const urlToBeRedirected = `https://${Cypress.env('FQDN')}/gazelle/testing/test/testExecution.seam?testingDepth=1&statusResult=null`

      // Browse url requires to be logged in
      cy.visit(urlToBeRedirected)

      // Check redirection to login page
      cy.location().should((loc) => {
        expect(loc.pathname).to.contains('/auth/realms')
      })
  
      // Login with correct password
      cy.local_login(`${Cypress.env('GAZELLE_ADMIN_USERNAME')}`, `${Cypress.env('GAZELLE_ADMIN_PASSWORD')}`)
  
      // Check redirection contains initial url with parameters
      cy.location().should((loc) => {
        expect(loc.href).to.contains("/gazelle/testing/test/testExecution.seam?")
        expect(loc.href).to.contains("testingDepth=1")
        expect(loc.href).to.contains("&statusResult=null")
      })
    })

    it.skip('Redirection in TM after manual login', () => {
      const urlToBeRedirected = `https://${Cypress.env('FQDN')}/gazelle/tf/actor/listActors.seam`

      // Browse url requires to be logged in
      cy.visit(urlToBeRedirected)

      cy.go_to_login_page()
      // Login with correct password
      cy.local_login(`${Cypress.env('GAZELLE_ADMIN_EMAIL')}`, `${Cypress.env('GAZELLE_ADMIN_PASSWORD')}`)
  
      // Check redirection to initial url
      cy.location().should((loc) => {
        expect(loc.href).to.equals(urlToBeRedirected)
      })
    })

    it.skip('Redirection in EVS after manual login', () => {
      const urlToBeRedirected = `https://${Cypress.env('FQDN')}/evs/administration/statistics.seam`

      // Browse url requires to be logged in
      cy.visit(urlToBeRedirected)

      cy.go_to_login_page()
      // Login with correct password
      cy.local_login(`${Cypress.env('GAZELLE_USER_EMAIL')}`, `${Cypress.env('GAZELLE_USER_PASSWORD')}`)
  
      // Check redirection to initial url
      cy.location().should((loc) => {
        expect(loc.href).to.equals(urlToBeRedirected)
      })
    })
})