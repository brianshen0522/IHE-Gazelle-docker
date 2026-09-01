package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.mailer.MockMailbox;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.vertx.ext.mail.MailMessage;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.interlay.user.UserResource;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import net.ihe.gazelle.user.management.quarkus.client.EditUserTestClient;
import net.ihe.gazelle.user.management.quarkus.utils.JSONMaker;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwtWithIdAndGroups;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.*;


abstract class AbstractEditUserControllerIT {

    public final String firstname = getFirstname();
    public final String lastname = getLastname();
    private final String userEmail = getMail();
    public static final String NEW_LASTNAME = "NEW-LASTNAME";
    public final String newEmail = getEmail();

    protected static String userID;
    protected static String orgaID;
    protected static String groupIdsJsonFormatted;
    private static GazelleIdentity mockedIdentity;
    private static EditUserTestClient editUserClient;
    private final Logger logger = LoggerFactory.getLogger(AbstractEditUserControllerIT.class);

    @Inject
    UserLookupService userLookupService;

    @Inject
    MockMailbox mailbox;
    protected String baseUsersPath = getBaseUsersPath();

    @BeforeAll
    static void setUp() {
        editUserClient = new EditUserTestClient();
        mockedIdentity = new MockedGazelleIdentity(Set.of(GazelleDefaultGroup.GAZELLE_ADMIN.getName()));
    }



    /**
     * Warning : The following tests are order dependent
     * Be careful when you are updating one of them
     */

    @Test
    @Order(0)
    void testCreateUserToEdit() {
        String body = JSONMaker.makeUserCreation(firstname, lastname, userEmail, getOrgaShortname(), getOrgaName());
        ExtractableResponse<Response> extractableResponse = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(body)
                .when().post(baseUsersPath + "/register")
                .then().statusCode(201).extract();

        userID = extractableResponse.body().jsonPath().getString("id");
        orgaID = extractableResponse.body().jsonPath().getString("organizationId");
        assertFalse(orgaID.isEmpty());
        assertNotEquals("3333-3333", orgaID);
        List<String> groupIds = extractableResponse.body().jsonPath().getList("groupIds");
        groupIdsJsonFormatted = listToJsonArray(groupIds);

        logger.info("User id : {}", userID);
    }



    @Test
    @Order(0)
    void testCreateUserToEditWithBadOrganization() {
        String body = JSONMaker.makeUserCreation(firstname, lastname, userEmail, null, getOrgaName());
        given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(body)
                .when().post(baseUsersPath+"/register")
                .then().statusCode(400);
    }

    @Test
    @Order(1)
    void testEditUserWithoutToken() {
        String body = "{\"lastName\":\"" + NEW_LASTNAME + "\", \"groupIds\":" + groupIdsJsonFormatted + "}";
        given()
                .header(HttpHeaders.ACCEPT_LANGUAGE, Locale.ENGLISH.getLanguage())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(body)
                .when()
                .patch(baseUsersPath +"/"+ userID)
                .then()
                .statusCode(401)
        ;
    }

    @Test
    @Order(1)
    void testEditUserSuccessful() {
        String jwt = getValidJwt();
        String body = "{\"lastName\":\"" + NEW_LASTNAME + "\", \"groupIds\":" + groupIdsJsonFormatted + "}";
        buildEditUserRequest(body, userID, jwt)
                .then()
                .statusCode(200)
                .body(containsString("\"firstName\":\"" + firstname + "\""))
                .body(containsString("\"lastName\":\"" + NEW_LASTNAME + "\""))
                .body(containsString("\"org-adm:" + orgaID + "\""))
        ;
    }

    @Test
    @Order(1)
    void testEditUserMail() {
        String jwt = getValidJwtWithIdAndGroups(userID, List.of("org:kereval"));
        String body = "{\"email\":\"" + newEmail + "\",\"groupIds\":" + groupIdsJsonFormatted + "}";
        buildEditUserRequest(body, userID, jwt)
                .then()
                .statusCode(200)
                .body(containsString("\"email\":\"" + newEmail + "\""))
                .body(containsString("\"firstName\":\"" + firstname + "\""))
                .body(containsString("\"org-adm:" + orgaID + "\""));

        List<MailMessage> mails = mailbox.getMailMessagesSentTo(newEmail);
        assertEquals(1, mails.size());
        MailMessage mailToNewEmail = mails.getFirst();
        assertTrue(mailToNewEmail.getText().contains("Your email address has been updated"));

        mails = mailbox.getMailMessagesSentTo(userEmail);
        assertFalse(mails.isEmpty());
        MailMessage mailToOldEmail = mails.getLast();
        assertTrue(mailToOldEmail.getText().contains("Your email address has been updated"));

        User user = userLookupService.getUserByEmail(newEmail, mockedIdentity);
        assertFalse(user.isActivated());
        assertNotNull(user.getActivationCode());
    }

    @Test
    @Order(1)
    void testEditUserBadUserId() {
        String jwt = getValidJwt();
        String body = "{\"lastName\":\"" + NEW_LASTNAME + "\"}";
        buildEditUserRequest(body, "badId", jwt)
                .then()
                .statusCode(404)
                .body(containsString("not found"));
    }

    @Test
    @Order(2)
    void testSearchUser() {
        String jwt = getValidJwt();
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .get(baseUsersPath+"/" + userID)
                .then()
                .statusCode(200)
                .body(containsString("\"firstName\":\"" + firstname + "\""))
                .body(containsString("\"lastName\":\"" + NEW_LASTNAME + "\""))
                .body(containsString("\"org-adm:" + orgaID + "\""))
                .body(containsString("\"org:" + orgaID + "\""))
        ;

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .get(baseUsersPath+"/" + "badId")
                .then()
                .statusCode(404)
                .body(containsString("not found"));
    }

    @Test
    @Order(2)
    void testActivateUserFromActivationCode() {
        String activationCode = userLookupService.getActivationCodeForUserId(userID);
        Response response = editUserClient.activateUserFromActivationCode(activationCode);

        assertEquals(200, response.statusCode());
        String body = response.getBody().print();
        assertNotNull(body);
        TextSerDes jacksonSerDes = new JacksonSerDes();
        assertDoesNotThrow(() -> jacksonSerDes.deserialize(body, UserResource.class));
        User receivedUser = jacksonSerDes.deserialize(body, UserResource.class).asUser();
        assertEquals(newEmail, receivedUser.getEmail());
        assertTrue(receivedUser.isActivated());

        response = editUserClient.activateUserFromActivationCode("bad_code");
        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(2)
    void testActivateUser() {
        Response response = editUserClient.activateUser(userID);
        assertEquals(200, response.statusCode());
        User user = userLookupService.getUserById(userID, mockedIdentity);
        assertTrue(user.isActivated());

        response = editUserClient.deactivateUser(null);
        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(2)
    void testDeactivateUser() {
        Response response = editUserClient.deactivateUser(userID);
        assertEquals(200, response.statusCode());
        User user = userLookupService.getUserById(userID, mockedIdentity);
        assertFalse(user.isActivated());

        response = editUserClient.deactivateUser(null);
        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(2)
    void testEditUserOrga() {
        String jwt = getValidJwt();
        String body = "{\"organizationId\":\"" + orgaID + "\"}";
        buildEditUserRequest(body, userID, jwt)
                .then()
                .statusCode(200)
                .body(containsString("\"lastName\":\"" + NEW_LASTNAME + "\""))
                .body(not(containsString("\"org-adm:KER3\"")))
                .body(not(containsString("\"org:KER3\"")))
                .body(containsString("\"org-adm:" + orgaID + "\""))
                .body(containsString("\"org:" + orgaID + "\""));
    }

    @Test
    @Order(3)
    void testDeleteUser() {
        Response response = editUserClient.deleteUser(userID);
        assertEquals(200, response.statusCode());

        List<MailMessage> mails = mailbox.getMailMessagesSentTo(newEmail);
        assertFalse(mails.isEmpty());
        MailMessage mailToDeletedAccount = mails.getLast();
        assertTrue(mailToDeletedAccount.getSubject().contains("Your account has been deleted"));
        assertThrows(NoSuchElementException.class, () -> userLookupService.getUserById(userID, mockedIdentity));

        // Check that the organization has been archived
        String jwt = getValidJwt();
        ExtractableResponse<Response> responseExtractableResponse = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .get("/rest/organizations/"+orgaID)
                .then().extract();
        boolean archived = responseExtractableResponse.body().jsonPath().getBoolean("archived");
        assertTrue(archived);
    }

    private Response buildEditUserRequest(String body, String userId, String jwt) {
        return given()
                .header(HttpHeaders.ACCEPT_LANGUAGE, Locale.ENGLISH.getLanguage())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(body)
                .when().patch(baseUsersPath+"/" + userId);
    }

    protected String listToJsonArray(List<String> list) {
        return "[" + list.stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(", ")) + "]";
    }

    protected abstract String getBaseUsersPath();

    protected String getFirstname() {
        return "FIRSTNAME";
    }

    protected String getLastname() {
        return "LASTNAME";
    }

    protected String getMail() {
        return "edit-user-controller@test.fr";
    }

    protected String getOrgaName() {
        return "Kereval3";
    }

    protected String getOrgaShortname() {
        return "KER3";
    }

    protected String getEmail() {
        return "new-email@test.fr";
    }
}
