
    describe('Test local login for Gazelle user', () => {
        before(() => {
            cy.readFile('data/remove_user.sql').then((sqlQuery) => {
                cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'cy_auth_user' });
                cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'cy_auth_admin' });
            });
            cy.readFile('data/before_correct_login.sql').then((sqlQuery) => {
                cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: null });
            });
        });

        after(() => {
            cy.readFile('data/remove_user.sql').then((sqlQuery) => {
                cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'cy_auth_user' });
                cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'cy_auth_admin' });
            });
        });

        // Test 1
        it('Authentication admin with username', () => {
            cy.readFile('data/remove_user.sql').then((sqlQuery) => {
                cy.task("connectGUMDBWithParameters",{ sqlQuery: sqlQuery, parameter: 'test' }).then(cy.log);
            });

          cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
          cy.go_to_login_page()

          cy.local_login('cy_auth_admin', 'password')
          cy.give_consent()
          cy.assert_is_logged_in_TM_as_admin()
        });

    // Test 2
    it('Authentication admin with email', () => {
      cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
      cy.go_to_login_page()
      
      cy.local_login('cy_auth_admin@gazelle.com', 'password')
      cy.assert_is_logged_in_TM_as_admin()
    });

    // Test 3
    it('Authentication user', () => {
        cy.go_to_gazelle_tool(`/gazelle`, "Gazelle")
        cy.go_to_login_page()
      
        cy.local_login('cy_auth_user', 'password')
        cy.give_consent()
        cy.assert_is_logged_in_TM()
    });
  })