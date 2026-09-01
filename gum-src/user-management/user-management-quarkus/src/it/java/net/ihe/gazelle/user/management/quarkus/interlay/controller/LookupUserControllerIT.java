package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.quarkus.utils.JSONMaker;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static org.apache.http.HttpHeaders.CONTENT_RANGE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(KeycloakMockResource.class)
class LookupUserControllerIT {

    public static final String BEARER = "Bearer ";
    private static final String GUM_REST_USERS = "/rest/users";
    private final Logger logger = LoggerFactory.getLogger(LookupUserControllerIT.class);
    private static final String FIRSTNAME = "FIRSTNAMEtwo";
    private static final String LASTNAME = "LASTNAMEtwo";
    private static final String USER_EMAIL = "lookup-user-controller@test.fr";
    private static String userID;
    private static String orgaID;

    @Inject
    UserRegistrationService userRegistrationService;

    @Test
    @Order(0)
    void createUserToEditTest() {
        String body = JSONMaker.makeUserCreation(FIRSTNAME, LASTNAME, USER_EMAIL,"KERLookupC", "KERLookupC name");
        ExtractableResponse<Response> extractableResponse = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(body)
                .when().post(GUM_REST_USERS + "/register")
                .then().statusCode(201).extract();

        userID = extractableResponse.body().jsonPath().getString("id");
        orgaID = extractableResponse.body().jsonPath().getString("organizationId");
    }

    @Test
    @Order(1)
    @TestSecurity(authorizationEnabled = false)
    void searchUsersWithoutToken() {
        given()
                .get(GUM_REST_USERS + "?search=lookup-user-controller&offset=0&limit=1")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(1)
    @TestSecurity(authorizationEnabled = false)
    void searchUsersTest() {
        String jwt = getValidJwt();
        logger.info("JWT: {}", jwt);
        given()
                .when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?search=lookup-user-controller&offset=0&limit=1")
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-1/1"))
                .body("users.firstName", hasItems(FIRSTNAME))
                .body("users.lastName", hasItems(LASTNAME))
                .body("users.delegated", hasItems(false))
                .body(containsString("org-adm:" + orgaID))
        ;
    }

    @Test
    @Order(1)
    void filterUsersTest() {
        String jwt = getValidJwt();
        given()
                .when().header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?firstName=" + FIRSTNAME)
                .then()
                .statusCode(200)
                .body("users.id", hasItems(userID))
                .body("users.email", hasItems(USER_EMAIL))
                .body("users.firstName", hasItems(FIRSTNAME))
                .body("users.lastName", hasItems(LASTNAME))
                .body(containsString("org-adm:"+ orgaID))
        ;
    }

    @Test
    @Order(1)
    void searchAndFilterUsersTest() {
        String jwt = getValidJwt();
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?search=" + LASTNAME + "&firstName=" + FIRSTNAME)
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-100/1"))
                .body("users.id", hasItems(userID))
                .body("users.email", hasItems(USER_EMAIL))
                .body("users.firstName", hasItems(FIRSTNAME))
                .body("users.lastName", hasItems(LASTNAME))
                .body(containsString("org-adm:"+ orgaID))
        ;
    }

    @Test
    @Order(1)
    void searchAndFilterUsersMultipleParamsTest() {
        String jwt = getValidJwt();
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?search=" + USER_EMAIL + "&firstName=" + FIRSTNAME + "&delegated=false&activated=false&lastName=" + LASTNAME)
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-100/1"))
                .body("users.id", hasItems(userID))
                .body("users.email", hasItems(USER_EMAIL))
                .body("users.firstName", hasItems(FIRSTNAME))
                .body("users.lastName", hasItems(LASTNAME))
                .body("users.delegated", hasItems(false))
                .body(containsString("org-adm:"+ orgaID))
        ;
    }

    @Test
    @Order(1)
    void searchAndFilterUsersSummaryTest() {
        String jwt = getValidJwt();
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "/summary?search=" + LASTNAME + "&firstName=" + FIRSTNAME)
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-100/1"))
                .body("users.id", hasItems(userID))
                .body("users.email", hasItems(nullValue()))
                .body("users.email", hasItems(nullValue()))
                .body("users.firstName", hasItems(FIRSTNAME))
                .body("users.lastName", hasItems(LASTNAME))
                .body("users.groups", hasItems(nullValue()))
        ;
    }

    @Test
    @Order(1)
    void searchAndFilterUsersSummaryUnauthenticatedTest() {
        given()
                .when()
                .get(GUM_REST_USERS + "/summary?search=" + LASTNAME + "&firstName=" + FIRSTNAME)
                .then()
                .statusCode(401)
        ;
    }

    @Test
    @Order(2)
    void filterUsersTestVariousCases() {
        String jwt = getValidJwt();

        User user1 = new User("id1", "FIRSTNAMEone", "LASTNAMEone", "filter-user1@test.fr", orgaID, new HashSet<>());
        User user2 = new User("id2", "FIRSTNAMEtwo", "LASTNAMEtwo", "filter-user2@test.fr", orgaID, new HashSet<>());


        User user3 = new User("id3", "FIRSTNAMEthree", "LASTNAMEthree", "filter-user3@test.fr", orgaID, new HashSet<>());
        User user4 = new User("id4", "FIRSTNAMEthree", "LASTNAMEtwo", "filter-user4@test.fr", orgaID, new HashSet<>());

        userRegistrationService.registerUser(user1, true, "Testtest1#", "Testtest1#", Locale.ENGLISH);
        userRegistrationService.registerUser(user2, true, "Testtest2#", "Testtest2#", Locale.ENGLISH);
        userRegistrationService.registerUser(user3, true, "Testtest3#", "Testtest3#", Locale.ENGLISH);
        userRegistrationService.registerUser(user4, true, "Testtest4#", "Testtest4#", Locale.ENGLISH);

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?firstName=FIRSTNAMEthree&limit=6")
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-6/2"))
                .body("users.firstName", hasItems("FIRSTNAMEthree"))
                .body("users.email", hasItems("filter-user4@test.fr", "filter-user3@test.fr"))
                .body("users.email", not(hasItems("filter-user1@test.fr", "filter-user2@test.fr")))
                .body("count", equalTo(2));

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?firstName=FIRSTNAMEthree&limit=2")
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-2/2"))
                .body("users.firstName", hasItems("FIRSTNAMEthree"))
                .body("users.email", hasItems("filter-user4@test.fr"))
                .body("users.email", not(hasItems("filter-user2@test.fr")))
                .body("count", equalTo(2));

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?firstName=FIRSTNAMEone&limit=1")
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-1/1"))
                .body("users.firstName", hasItems("FIRSTNAMEone"))
                .body("users.email", hasItems("filter-user1@test.fr"))
                .body("users.email", not(hasItems("filter-user2@test.fr")))
                .body("count", equalTo(1));

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?organizationId="+orgaID)
                .then()
                .statusCode(200)
                .body("users.firstName", hasItems("FIRSTNAMEone"))
                .body("users.email", hasItems("filter-user1@test.fr", "filter-user2@test.fr"))
                .body("count", greaterThanOrEqualTo(2));

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS)
                .then()
                .statusCode(200)
                .body("users.email", hasItems("filter-user1@test.fr", "filter-user2@test.fr",
                        "filter-user3@test.fr", "filter-user4@test.fr"))
                .body("count", greaterThanOrEqualTo(5));

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?delegated=true")
                .then()
                .statusCode(200)
                .body("count", greaterThanOrEqualTo(0));

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?organizationId=notExistingOrga")
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-100/0"))
                .body(containsString("[]"))
                .body("count", equalTo(0));

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?group=org-adm:"+orgaID)
                .then()
                .statusCode(200)
                .body("users.email", hasItems(USER_EMAIL))
                .body("count", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    void filterUsersTestWithFalseGroup() {
        String jwt = getValidJwt();
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwt)
                .get(GUM_REST_USERS + "?group=jean_peuplu_group")
                .then()
                .statusCode(200)
                .header(CONTENT_RANGE, containsString("users 0-100/0"))
                .body(containsString("[]"))
                .body("count", equalTo(0));
    }
}
