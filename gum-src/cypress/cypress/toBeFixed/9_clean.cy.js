
describe('Clean environment after tests', () => {

    it.skip('Update SMTP config to prod config', () => {
        cy.UpdateSMTPConfig("localhost", "1025");
    });
})