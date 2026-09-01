package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.vertx.ext.mail.MailMessage;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.quarkus.utils.JSONMaker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static net.ihe.gazelle.user.management.quarkus.interlay.controller.LookupUserControllerIT.BEARER;
import static net.ihe.gazelle.user.management.quarkus.interlay.controller.user.UserControllerImpl.COULD_NOT_REGISTER_USER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(KeycloakMockResource.class)
class RegisterUserControllerV1IT{
    private static final Logger logger = LoggerFactory.getLogger(RegisterUserControllerV1IT.class);
    private static String orgaID;

    @Inject
    ApplicationConfig applicationConfig;
    @Inject
    MockMailbox mailbox;
    @Inject
    ConsentService consentService;

    private final String baseUserPath = getUsersBasePath();


    /**
     * Warning : The following tests are order dependent
     * Be careful when you are updating one of them
     */

    @ParameterizedTest
    @MethodSource("provideBadBody")
    void testBadRegisterUsers(String body) {
        buildRegisterUserRequest(body)
                .then()
                .statusCode(400)
                .body(containsString(COULD_NOT_REGISTER_USER))
                .body(containsString("is null"));
    }

    static Stream<Arguments> provideBadBody() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("{}"),
                Arguments.of("{\"firstName\":\"FIRSTNAME\",\"email\":\"testBadRegisterUsers@test.fr\",\"organizationId\":\"KER\"}"),
                Arguments.of("{\"lastName\":\"LASTNAME\",\"firstName\":\"FIRSTNAME\",\"organizationId\":\"KER\"}"),
                Arguments.of("{\"lastName\":\"LASTNAME\",\"firstName\":\"FIRSTNAME\",\"email\":\"testBadRegisterUsers@test.fr\"}")
        );
    }

    @Test
    @Order(0)
    void testRegisterUsersCreateOrganization() {
        String body = JSONMaker.makeUserCreation("FIRSTNAME", "LASTNAME", "test@test2-v1.fr", "KERRegisterC3", "Kereval3V1");

        Response response = buildRegisterUserRequest(body);
        ExtractableResponse<Response> extractableResponse = response.then().statusCode(201)
                .body(containsString("\"firstName\":\"FIRSTNAME\""))
                .body(containsString("\"lastName\":\"LASTNAME\""))
                .body(containsString("\"activated\":false"))
                .body(containsString("\"role:sut_operator\"")).extract();

        orgaID = extractableResponse.body().jsonPath().getString("organizationId");
        assertEquals(orgaID, extractableResponse.body().jsonPath().getString("organizationId"));

        // Check sent email
        List<MailMessage> mailMessages = mailbox.getMailMessagesSentTo("test@test2-v1.fr");
        assertFalse(mailMessages.isEmpty());
        String textContent = mailMessages.getFirst().getText();
        assertTrue(textContent.contains("LASTNAME"));
        assertTrue(textContent.contains("FIRSTNAME"));
        assertTrue(textContent.contains("Kereval3"));
        assertTrue(textContent.contains("/validate/"));
        assertConsentIsGiven(response, true);
    }

    @Test
    void testCreateUserByAdmin() {
        String body = JSONMaker.makeUserRegistration("FIRSTNAME CREATION", "LASTNAME CREATION", "test@test.creation-v1.fr", orgaID);

        Response response = buildCreateUserRequest(body);
        response.then().statusCode(201)
                .body(containsString("\"firstName\":\"FIRSTNAME CREATION\""))
                .body(containsString("\"lastName\":\"LASTNAME CREATION\""))
                .body(containsString("\"email\":\"test@test.creation-v1.fr\""))
                .body(containsString("\"activated\":true"))
                .body(containsString("role:sut_operator"))
                .body(containsString("org:" + orgaID));
        assertConsentIsGiven(response, false);

        List<MailMessage> mailMessages = mailbox.getMailMessagesSentTo("test@test.creation-v1.fr");

        // Avoid the test to fail if depending on the local GZL_USER_CREATION_EMAIL_NOTIFICATION_ENABLED .env value. This
        // is not a "hack" to make the test pass every time.
        // If the value is true then we check that an email is sent, if false we check that no email is sent.
        // It would be better to have a way to change the env variable directly in the test, but it is complicated as it
        // is an integration test with deployed app.
        if (applicationConfig.isUserCreationEmailNotificationEnabled()) {
            // Check sent email
            assertFalse(mailMessages.isEmpty());
            String textContent = mailMessages.getFirst().getText();
            assertTrue(textContent.contains("LASTNAME"));
            assertTrue(textContent.contains("FIRSTNAME"));
            assertTrue(textContent.contains("Gazelle administrator just created you an account"));
            assertTrue(textContent.contains("/login-actions/reset-credentials?client_id=gazelle-account"));
        } else {
            // Check no email sent
            assertTrue(mailMessages.isEmpty());
        }
    }

    private void assertConsentIsGiven(Response response, boolean consentGiven) {
        Pattern pattern = Pattern.compile("\"id\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(response.body().asString());
        if (!matcher.find())
            fail("No id found");

        if (consentGiven)
            assertFalse(consentService.needToGiveConsent(matcher.group(1)));
        else
            assertTrue(consentService.needToGiveConsent(matcher.group(1)));
    }

    @Test
    void testRegisterUserWithUselessAttributes() {
        String body = "{ \"id\":\"userID\", \"lastName\":\"LASTNAME THREE\"," +
                " \"firstName\":\"FIRSTNAME THREE\", \"email\":\"test@test6-v1.fr\"," +
                " \"password\" : \"Password47&\"," + " \"passwordConfirmation\" : \"Password47&\", " +
                " \"organizationId\":\"" + orgaID + "\", \"groups\":[ \"admin_role\" ]," +
                " \"activated\":true,\"lastLoginTimestamp\":5 }";

        buildRegisterUserRequest(body).then().statusCode(201)
                .body(not(containsString("\"id\":\"userID\"")))
                .body(containsString("\"firstName\":\"FIRSTNAME THREE\""))
                .body(containsString("\"lastLoginTimestamp\":0"))
                .body(containsString("\"activated\":false"))
                .body(containsString("role:sut_operator"))
                .body(containsString("org:" + orgaID));
    }

    @Test
    @Order(1)
    void testRegisterUserNotCorrect() {
        // Email already exist
        String body = JSONMaker.makeUserRegistration("FIRSTNAME SECOND", "LASTNAME SECOND", "test@test2-v1.fr", orgaID);
        buildRegisterUserRequest(body)
                .then()
                .statusCode(400)
                .body(containsString("Email already exist"));

        // Organization already exist
        String body2 = JSONMaker.makeUserCreation("FIRSTNAME FOUR", "LASTNAME FOUR", "test@test4-v1.fr",
                "KERRegisterC3", "Kereval3");

        buildRegisterUserRequest(body2)
                .then()
                .statusCode(409)
                .body(containsString("Organization"))
                .body(containsString("already exist"));
    }

    @Test
    @Order(2)
    void testActivateUser() {
        // Check sent email
        List<MailMessage> mailMessages = mailbox.getMailMessagesSentTo("test@test2-v1.fr");
        assertFalse(mailMessages.isEmpty());

        // Extract activation link
        String textContent = mailMessages.getFirst().getText();
        Pattern urlPattern = Pattern.compile("validate/([a-z0-9]+)");
        Matcher matcher = urlPattern.matcher(textContent);
        if (!matcher.find())
            fail("No activation link found");
        String activationCode = matcher.group(1);

        logger.info("Activation code: {}", activationCode);
        given()
                .when().post(baseUserPath+"/activate/" + activationCode)
                .then()
                .statusCode(200)
                .body(containsString("\"firstName\":\"FIRSTNAME\""))
                .body(containsString("\"activated\":true"))
        ;
    }

    @Test
    @Order(2)
    void testBadActivationCodeUser() {
        given()
                .when().post(baseUserPath+"/activate/badActivationCode")
                .then()
                .statusCode(400)
                .body(containsString("error"));
    }

    @Test
    @Order(3)
    void testRegisterUserJoinOrganization() {
        String body = JSONMaker.makeUserRegistration("FIRSTNAME SECOND", "LASTNAME SECOND", "test@test3-v1.fr", orgaID);

        Response response = buildRegisterUserRequest(body);
        response.then().statusCode(201)
                .body(containsString("\"firstName\":\"FIRSTNAME SECOND\""))
                .body(containsString("\"lastName\":\"LASTNAME SECOND\""))
                .body(containsString("\"email\":\"test@test3-v1.fr\""))
                .body(containsString("\"activated\":false"))
                .body(containsString("role:sut_operator"))
                .body(containsString("org:" + orgaID));

        // Check sent email
        List<MailMessage> mailMessages = mailbox.getMailMessagesSentTo("test@test2-v1.fr");
        assertTrue(!mailMessages.isEmpty());

        String textContent = mailMessages.getLast().getText();
        assertTrue(textContent.contains("LASTNAME SECOND"));
        assertTrue(textContent.contains("FIRSTNAME SECOND"));
        assertTrue(textContent.contains("test@test3-v1.fr"));
        assertTrue(textContent.contains("Kereval3"));
        assertTrue(textContent.contains("/validate/"));

        assertConsentIsGiven(response, true);
    }


    @Test
    void testSearchAllUsers() {
        String jwt = getValidJwt();
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(baseUserPath)
                .then()
                .statusCode(200)
                .body(containsString("["))
                .body(containsString("]"));
    }

    private Response buildRegisterUserRequest(String body) {
        return given()
                .header(HttpHeaders.ACCEPT_LANGUAGE, Locale.ENGLISH.getLanguage())
                .header(org.apache.http.HttpHeaders.ACCEPT_LANGUAGE, Locale.ENGLISH.getLanguage())
                .header(HttpHeaders.CONTENT_TYPE, jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .body(body)
                .when().post(getUsersBasePath() + "/register");
    }



    private Response buildCreateUserRequest(String body) {
        String jwt = getValidJwt();
        return given()
                .header(HttpHeaders.ACCEPT_LANGUAGE, Locale.ENGLISH.getLanguage())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .body(body)
                .when().post(getUsersBasePath());
    }

    protected String getUsersBasePath() {
        return "/rest/users";
    }
}
