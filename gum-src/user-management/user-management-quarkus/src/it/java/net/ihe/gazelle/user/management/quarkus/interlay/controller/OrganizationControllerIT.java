package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.user.management.quarkus.utils.JSONMaker;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static net.ihe.gazelle.user.management.quarkus.interlay.controller.organization.OrganizationController.ORGANIZATION_REST_PATH;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@QuarkusTestResource(KeycloakMockResource.class)
class OrganizationControllerIT {

    private static final String PREFIX = "OrgaCtrlIT";
    private static final String BEARER = "Bearer ";
    private static final String LAST_UPDATE_TIMESTAMP = "lastUpdateTimestamp";
    private static final String DELEGATED = "delegated";
    private static final String ARCHIVED = "archived";
    private static String organizationId;
    private final String jwtHeader = BEARER + getValidJwt();
    private static Long lastUpdateTimestamp;
    /**
     * Warning : The following tests are order dependent
     * Be careful when you are updating one of them
     */

    @Test
    @Order(0)
    void createOrganizationForPatch() {
        String organization = JSONMaker.makeOrganization(PREFIX + "-SN-PATCH", PREFIX + "-patch-org");

        organizationId = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .body(organization)
                .when().post(ORGANIZATION_REST_PATH)
                .then().statusCode(201)
                .extract().body().jsonPath().getString("id");
    }


    @Test
    @Order(1)
    void createOrganizationConflict() {
        String shortNameUpper = (PREFIX + "-SN-PATCH").toUpperCase();
        String organization = JSONMaker.makeOrganization(shortNameUpper, PREFIX + "-patch-org-other");
        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .body(organization)
                .when().post(ORGANIZATION_REST_PATH)
                .then().statusCode(409);

        String nameUpper = (PREFIX + "-patch-org").toUpperCase();
        String organization2 = JSONMaker.makeOrganization(PREFIX + "-SN-PATCH", nameUpper);
        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .body(organization2)
                .when().post(ORGANIZATION_REST_PATH)
                .then().statusCode(409);
    }

    @Test
    @Order(1)
    void testPatchOrganizationWithoutToken() {
        String updateBody = JSONMaker.makeOrganization(PREFIX + "-SN-PATCH", PREFIX + "-name-patched");

        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(updateBody)
                .when().patch(ORGANIZATION_REST_PATH + "/" + organizationId)
                .then()
                .statusCode(401);
    }

    @Test
    @Order(1)
    void testGetOrganizationSuccessfully() {
        ExtractableResponse<Response> response = given()
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .when().get(ORGANIZATION_REST_PATH + "/" + organizationId)
                .then()
                .statusCode(200).extract();
        JsonPath jsonPath = response.body().jsonPath();
        assertEquals(organizationId, jsonPath.get("id"));
        assertFalse(jsonPath.getBoolean(DELEGATED));
        assertFalse(jsonPath.getBoolean(ARCHIVED));
        assertTrue(jsonPath.getLong(LAST_UPDATE_TIMESTAMP) > 0);
        lastUpdateTimestamp = jsonPath.getLong(LAST_UPDATE_TIMESTAMP);
    }

    @Test
    @Order(2)
    void testPatchOrganizationSuccessfully() throws InterruptedException {
        String updateBody = JSONMaker.makeOrganization(PREFIX + "-SN-PATCH", PREFIX + "-name-patched");
        ExtractableResponse<Response> response = given()
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(updateBody)
                .when().patch(ORGANIZATION_REST_PATH + "/" + organizationId)
                .then()
                .statusCode(200).extract();
        JsonPath jsonPath = response.body().jsonPath();
        assertEquals(organizationId, jsonPath.get("id"));
        assertEquals(PREFIX + "-name-patched", jsonPath.get("name"));
        assertFalse(jsonPath.getBoolean(DELEGATED));
        assertFalse(jsonPath.getBoolean(ARCHIVED));
        assertTrue(jsonPath.getLong(LAST_UPDATE_TIMESTAMP) > 0);
        assertNotEquals(lastUpdateTimestamp, jsonPath.getLong(LAST_UPDATE_TIMESTAMP));
        lastUpdateTimestamp = jsonPath.getLong(LAST_UPDATE_TIMESTAMP);
    }

    @Test
    @Order(3)
    void testArchiveOrganizationSuccessfully() {
        given()
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .when().delete(ORGANIZATION_REST_PATH + "/" + organizationId)
                .then()
                .statusCode(200);

        ExtractableResponse<Response> response = given()
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body("{}")
                .when().patch(ORGANIZATION_REST_PATH + "/" + organizationId)
                .then()
                .statusCode(200).extract();
        JsonPath jsonPath = response.body().jsonPath();
        assertEquals(organizationId, jsonPath.get("id"));
        assertFalse(jsonPath.getBoolean(DELEGATED));
        assertTrue(jsonPath.getBoolean(ARCHIVED));
        assertNotEquals(lastUpdateTimestamp, jsonPath.getLong(LAST_UPDATE_TIMESTAMP));
        lastUpdateTimestamp = jsonPath.getLong(LAST_UPDATE_TIMESTAMP);
    }
}
