 //const { forEach } = require("cypress/types/lodash")
 
 describe('template spec', () => {

  it.skip('passes', () => {
    cy.visit(`https://${Cypress.env('FQDN')}/auth`)
    cy.get('h1').contains('Welcome to Keycloak')
  })

  
  it.skip('Authentification', () => {
    cy.fixture('app').then((app) =>{ 

      var genArr = Array.from({length:3},(v,k)=>k+1)
      cy.wrap(genArr).each((indexloop) => { 
        var index= indexloop-1

        //Visite EVS
        cy.visit_tool(`${app.PATH[index]}`,`${app.NAVBAR_BRAND[index]}`,`${app.CHECK_AUTH_LOC[index]}`)

        // CO
        cy.local_login(`${Cypress.env('GAZELLE_ADMIN_USERNAME')}`,`${Cypress.env('GAZELLE_ADMIN_PASSWORD')}`)

        // Deco
        cy.logout(`${app.CHECK_LOGIN[index]}`,`${app.LOGOUT_BUTTON_ID[index]}`)
      })

    })
  })

  it.skip('Redirection Link', () => {
    cy.fixture('app').then((app) =>{
      //Redirection
      cy.Redirection(`${Cypress.env('GAZELLE_ADMIN_USERNAME')}`,`${Cypress.env('GAZELLE_ADMIN_PASSWORD')}`)
      
    })
  })

  it.skip('Force Block', () => {
    cy.fixture('app').then((app) =>{
      //Force Block
      cy.ForceBlock(`${Cypress.env('GAZELLE_ADMIN_USERNAME')}`,`${Cypress.env('GAZELLE_ADMIN_PASSWORD')}`)
      
    })
  })
})