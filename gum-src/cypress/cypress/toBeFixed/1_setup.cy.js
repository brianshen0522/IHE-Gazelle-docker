
describe('Setup environment', () => {

    it.skip('Update SMTP config to catch emails', () => {
        cy.UpdateSMTPConfig(`${Cypress.env('MAILDEV_HOST')}`, `${Cypress.env('MAILDEV_SMTP_PORT')}`, "test", "test");
        //TODO update locales for users
    });
})