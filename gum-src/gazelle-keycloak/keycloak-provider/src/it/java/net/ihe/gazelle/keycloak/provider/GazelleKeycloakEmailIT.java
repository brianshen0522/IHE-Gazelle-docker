package net.ihe.gazelle.keycloak.provider;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.keycloak.provider.utils.KeycloakAdminOperator;
import net.ihe.gazelle.keycloak.provider.utils.MailBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class GazelleKeycloakEmailIT {

    private static RealmResource gazelleRealm;
    private static MailBox mailBox;

    @BeforeAll
    public static void setUp() {
        KeycloakAdminOperator keycloakAuthentication = new KeycloakAdminOperator();
        gazelleRealm = keycloakAuthentication.getRealmGazelle();
        mailBox = new MailBox();

        // Set email for admin user (required to test email sending)
        Keycloak keycloak = keycloakAuthentication.getKeycloakInstance();
        UsersResource usersResource = keycloak.realm("master").users();
        UserRepresentation admin = usersResource.searchByUsername("admin",true).get(0);
        admin.setEmail("test@test.com");
        usersResource.get(admin.getId()).update(admin);
    }

    @Test
    void testMailSend() {
        Map<String, String> smtpConfiguration = gazelleRealm.toRepresentation().getSmtpServer();
        Response response = gazelleRealm.testSMTPConnection(smtpConfiguration);
        assertEquals(204, response.getStatus());

        JsonNode lastMailJson = mailBox.getLastMailAsJson();
        assertNotNull(lastMailJson, "No mail received");
        String subjectEmail = lastMailJson.get("Headers").get("Subject").get(0).asText();
        assertTrue(subjectEmail.contains("[KEYCLOAK] - SMTP test message"));
    }

    @Test
    void askForResetPasswordMail() {
        UsersResource usersResource = gazelleRealm.users();
        String userResourceId = usersResource.searchByEmail("user2@gazelle.com", true).get(0).getId();
        UserResource userResource = usersResource.get(userResourceId);
        userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));

        JsonNode lastMailJson = mailBox.getLastMailAsJson();
        assertNotNull(lastMailJson, "No mail received");
        String subjectEmail = lastMailJson.get("Headers").get("Subject").get(0).asText();
        String fromEmail = lastMailJson.get("Headers").get("From").get(0).asText();
        String bodyEmail = lastMailJson.get("Body").asText();
        assertEquals("Update Your Account",subjectEmail);
        assertEquals("no-reply@localhost",fromEmail);
        assertTrue(bodyEmail.contains("Update Password"));
    }

    @Test
    void verifyEmail() {
        UsersResource usersResource = gazelleRealm.users();
        String userResourceId = usersResource.searchByEmail("user2@gazelle.com", true).get(0).getId();
        UserResource userResource = usersResource.get(userResourceId);
        userResource.executeActionsEmail(List.of("VERIFY_EMAIL"));

        JsonNode lastMailJson = mailBox.getLastMailAsJson();
        assertNotNull(lastMailJson, "No mail received");
        String subjectEmail = lastMailJson.get("Headers").get("Subject").get(0).asText();
        String fromEmail = lastMailJson.get("Headers").get("From").get(0).asText();
        String toEmail = lastMailJson.get("Headers").get("To").get(0).asText();
        String bodyEmail = lastMailJson.get("Body").asText();

        assertEquals("Update Your Account",subjectEmail);
        assertEquals("no-reply@localhost",fromEmail);
        assertEquals("user2@gazelle.com",toEmail);
        assertTrue(bodyEmail.contains("Verify Email"));
    }

    @Test
    void allMailAreRead() {
        JsonNode lastMailJson = mailBox.getLastMailAsJson();
        assertNull(lastMailJson, "No mail should remains in mailbox");
    }
}
