package net.ihe.gazelle.keycloak.provider;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.keycloak.provider.utils.KeycloakAdminOperator;
import net.ihe.gazelle.keycloak.provider.utils.MailBox;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.junit.jupiter.api.*;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GazelleKeycloakUserIT {

    public static final String USER_EMAIL = "testcreateuserfromkeycloak@test.fr";
    private static KeycloakAdminOperator keycloakAdminOperator;
    private static RealmResource realmGazelle;

    private static MailBox mailBox;

    @BeforeAll
    public static void setUp() {
        keycloakAdminOperator = new KeycloakAdminOperator();
        realmGazelle = keycloakAdminOperator.getRealmGazelle();
        mailBox = new MailBox();
    }

    @Test
    @Order(0)
    void testCreateUserFromKeycloak() {
        // Create user
        User user = new User("test");
        user.setFirstName("test firstname");
        user.setLastName("test lastname");
        user.setEmail(USER_EMAIL);
        Response response = keycloakAdminOperator.addNewUser(user);
        assertEquals(201,response.getStatus());

        // Retrieve created user
        List<UserRepresentation> userRepresentationList = realmGazelle.users().searchByEmail(USER_EMAIL,true);
        assertEquals(1,userRepresentationList.size());
        UserRepresentation userRepresentation = userRepresentationList.get(0);
        assertEquals("test firstname",userRepresentation.getFirstName());
        assertEquals("test lastname",userRepresentation.getLastName());
    }

    @Test
    @Order(1)
    void testEmailSentForUserCreation() {
        JsonNode lastMail = mailBox.getLastMailAsJson();
        assertNotNull(lastMail, "An email should be sent");
        String subjectMail = lastMail.get("Headers").get("Subject").get(0).asText();
        EmailBody emailBody = new EmailBody(lastMail.get("Body").asText());
        assertTrue(subjectMail.contains("Gazelle account has been created"));
        assertTrue(emailBody.getBodyPlainText().contains("Dear test firstname test lastname"),
                "The email body plain text should contain user firstname and lastname");
        assertTrue(emailBody.getBodyPlainText().contains("/realms/gazelle/login-actions/reset-credentials"),
                "The email body plain text should contain the reset password link");
        assertTrue(emailBody.getBodyHtml().contains("Dear test firstname test lastname"),
                "The email body html should contain user firstname and lastname");
        assertTrue(emailBody.getBodyHtml().contains("/realms/gazelle/login-actions/reset-credentials"),
                "The email body html should contain the reset password link");
    }

    @Test
    @Order(1)
    void testEditUserAttributesFromKeycloak() {
        // Retrieve user
        List<UserRepresentation> userRepresentationList = realmGazelle.users().searchByEmail(USER_EMAIL,true);
        assertEquals(1,userRepresentationList.size());
        UserRepresentation userRepresentation = userRepresentationList.get(0);

        // Edit user
        userRepresentation.setFirstName("testEditUserFromKeycloak firstname");
        userRepresentation.setLastName("testEditUserFromKeycloak lastname");
        userRepresentation.setEnabled(true);
        realmGazelle.users().get(userRepresentation.getId()).update(userRepresentation);

        // Retrieve updated user
        userRepresentationList = realmGazelle.users().searchByEmail(USER_EMAIL,true);
        assertEquals(1,userRepresentationList.size());
        userRepresentation = userRepresentationList.get(0);
        assertEquals("testEditUserFromKeycloak firstname",userRepresentation.getFirstName());
        assertEquals("testEditUserFromKeycloak lastname",userRepresentation.getLastName());
        assertTrue(userRepresentation.isEnabled());
    }

    @Test
    @Order(2)
    void testDeleteUserFromKeycloak() {
        List<UserRepresentation> userRepresentationList = realmGazelle.users().searchByEmail(USER_EMAIL, true);
        assertEquals(1, userRepresentationList.size());
        UserRepresentation userRepresentation = userRepresentationList.get(0);
        realmGazelle.users().get(userRepresentation.getId()).remove();

        // Check that user is well deleted
        userRepresentationList = realmGazelle.users().searchByEmail(USER_EMAIL,true);
        assertEquals(0,userRepresentationList.size());
    }
}
