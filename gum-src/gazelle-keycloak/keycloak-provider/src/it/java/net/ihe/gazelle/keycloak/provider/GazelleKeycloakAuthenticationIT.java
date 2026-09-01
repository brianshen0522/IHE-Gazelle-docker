package net.ihe.gazelle.keycloak.provider;

import net.ihe.gazelle.keycloak.provider.utils.KeycloakAdminOperator;
import net.ihe.gazelle.keycloak.provider.utils.MailBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.shaded.org.awaitility.Durations;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GazelleKeycloakAuthenticationIT {

    private static KeycloakAdminOperator keycloakAdminOperator;
    private static RealmResource realmGazelle;
    private static MailBox mailBox;

    @BeforeAll
    public static void setUp() {
        keycloakAdminOperator = new KeycloakAdminOperator();
        realmGazelle = keycloakAdminOperator.getRealmGazelle();

        mailBox = new MailBox();

        // Update client account to allow direct access grant
        ClientRepresentation clientRepresentation = realmGazelle.clients().findByClientId("account").get(0);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        realmGazelle.clients().get(clientRepresentation.getId()).update(clientRepresentation);
    }

    @Test
    void normalCorrectAuthenticationTest() {
        // Test MD5 correct password
        HttpResponse<String> httpResponse = keycloakAdminOperator.logInUser("user3", "aZeRtY");
        assertEquals(200, httpResponse.statusCode());

        // Test PBKDF2 correct password
        httpResponse = keycloakAdminOperator.logInUser("user2", "password");
        assertEquals(200, httpResponse.statusCode());
    }

    @Test
    void normalBadAuthenticationTest() {
        // Test MD5 bad password
        HttpResponse<String> httpResponse = keycloakAdminOperator.logInUser("user1", "badPassword");
        assertEquals(400, httpResponse.statusCode());
        assertTrue(httpResponse.body().contains("Invalid user credentials"));

        // Test PBKDF2 bad password
        httpResponse = keycloakAdminOperator.logInUser("user2", "badPassword");
        assertEquals(400, httpResponse.statusCode());
        assertTrue(httpResponse.body().contains("Invalid user credentials"));

        // Test with bad username
        httpResponse = keycloakAdminOperator.logInUser("badUsername", "badPassword");
        assertEquals(400, httpResponse.statusCode());
        assertTrue(httpResponse.body().contains("Invalid user credentials"));
    }

    @Test
    void noActivationCodeAuthenticationTest() {
        // Try to log in with an inactive user
        HttpResponse<String> httpResponse = keycloakAdminOperator.logInUser("inactiveUser", "aZeRtY");
        assertEquals(400, httpResponse.statusCode());
        assertTrue(httpResponse.body().contains("Account disabled"));

        // Check email sent to the user
        JsonNode lastMail = mailBox.getLastMailAsJson();
        assertNull(lastMail, "No email should be sent");
    }

    @Test
    void noConsentGivenAuthenticationTest() {
        // Try to log in with a user who has not given his consent
        HttpResponse<String> httpResponse = keycloakAdminOperator.logInUser("user4@gazelle.com", "aZeRtY");
        assertEquals(400, httpResponse.statusCode());
        assertTrue(httpResponse.body().contains("Account is not fully set up"));

        // Check no email sent
        JsonNode lastMail = mailBox.getLastMailAsJson();
        assertNull(lastMail, "No email should be sent");
    }

    @Test
    void inactiveCorrectAuthenticationTest() {
        // Try to log in with an inactive user
        HttpResponse<String> httpResponse = keycloakAdminOperator.logInUser("inactiveUser2", "aZeRtY");
        assertEquals(400, httpResponse.statusCode());
        assertTrue(httpResponse.body().contains("Account disabled"));

        // Check email sent to the user
        JsonNode lastMail = mailBox.getLastMailAsJson();
        assertNotNull(lastMail, "An email should be sent");
        String subjectMail = lastMail.get("Headers").get("Subject").get(0).asText();
        EmailBody emailBody = new EmailBody(lastMail.get("Body").asText());
        assertTrue(subjectMail.contains("not active yet"));
        assertTrue(emailBody.getBodyPlainText().contains("Dear inactive2_firstname inactive2_lastname"),
                "The email body plain text should contain user firstname and lastname");
        assertFalse(emailBody.getBodyPlainText().contains("confirmRegistration.seam?activationCode=code_activation"),
                "The email body plain text should not contain the activation link");
        assertTrue(emailBody.getBodyHtml().contains("Dear inactive2_firstname inactive2_lastname"),
                "The email body html should contain user firstname and lastname");
        assertFalse(emailBody.getBodyHtml().contains("confirmRegistration.seam?activationCode&#61;code_activation"),
                "The email body html should not contain the activation link");
    }

    @Test
    void bruteForceAuthenticationTest() {
        // Attempt to log in 6 times with a wrong password
        for (int i = 0; i < 6; i++) {
            HttpResponse<String> httpResponse = keycloakAdminOperator.logInUser("user2", "badPassword");
            assertEquals(400, httpResponse.statusCode());
            assertTrue(httpResponse.body().contains("Invalid user credentials"));

            // Wait 1 second between each attempt
            Awaitility.await().pollDelay(Durations.ONE_SECOND).until(() -> true);
        }

        // Check user is temporary locked
        UserRepresentation userRepresentation = realmGazelle.users().searchByEmail("user2@gazelle.com", true).get(0);
        Map<String, Object> bruteForceStatus = realmGazelle.attackDetection().bruteForceUserStatus(userRepresentation.getId());
        assertTrue((boolean) bruteForceStatus.get("disabled"));
        assertTrue(userRepresentation.getAttributes().containsKey("NOTIFIED_BLOCKED_ACCOUNT"));

        // Check email sent to the user
        JsonNode lastMail = mailBox.getLastMailAsJson();
        assertNotNull(lastMail);
        String subjectMail = lastMail.get("Headers").get("Subject").get(0).asText();
        EmailBody emailBody = new EmailBody(lastMail.get("Body").asText());
        assertTrue(subjectMail.contains("blocked"));
        assertTrue(emailBody.getBodyPlainText().contains("Dear user2 fn user2 ln"));
        assertTrue(emailBody.getBodyHtml().contains("Dear user2 fn user2 ln"));

        assertTrue(emailBody.getBodyPlainText().contains("realms/gazelle/login-actions/reset-credentials"),
                "The email body plain text should contain link to reset credentials");
        assertTrue(emailBody.getBodyHtml().contains("realms/gazelle/login-actions/reset-credentials"),
                "The email body html should contain link to reset credentials");
        // Try another attempt to log in
        HttpResponse<String> httpResponse = keycloakAdminOperator.logInUser("user2", "badPassword");
        assertEquals(400, httpResponse.statusCode());
        assertTrue(httpResponse.body().contains("Invalid user credentials"));

        // Check no email sent to the user
        lastMail = mailBox.getLastMailAsJson();
        assertNull(lastMail);

        // Clear brute force attack for user
        realmGazelle.attackDetection().clearBruteForceForUser(userRepresentation.getId());
    }

    @Test
    void inactiveVendorAdminCorrectAuthenticationTest() {
        // Try to log in with an inactive user
        HttpResponse<String> httpResponse = keycloakAdminOperator.logInUser("inactiveVendor", "aZeRtY");
        assertEquals(400, httpResponse.statusCode());
        assertTrue(httpResponse.body().contains("Account disabled"));

        // Check email sent to the user
        JsonNode lastMail = mailBox.getLastMailAsJson();
        assertNotNull(lastMail, "An email should be sent");
        String subjectMail = lastMail.get("Headers").get("Subject").get(0).asText();
        EmailBody emailBody = new EmailBody(lastMail.get("Body").asText());
        assertTrue(subjectMail.contains("not active yet"));
        assertTrue(emailBody.getBodyPlainText().contains("Dear inactiveVendor_firstname inactiveVendor_lastname"),
                "The email body plain text should contain user firstname and lastname");
        assertTrue(emailBody.getBodyPlainText().contains("/validate/code_activation"),
                "The email body plain text should contain the activation link");
        assertTrue(emailBody.getBodyHtml().contains("Dear inactiveVendor_firstname inactiveVendor_lastname"),
                "The email body html should contain user firstname and lastname");
        assertTrue(emailBody.getBodyHtml().contains("code_activation"),
                "The email body html should contain the activation link" + emailBody.getBodyHtml());
    }
}
