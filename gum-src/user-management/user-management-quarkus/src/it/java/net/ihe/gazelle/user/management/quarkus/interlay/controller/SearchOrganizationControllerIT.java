/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@QuarkusTestResource(KeycloakMockResource.class)
class SearchOrganizationControllerIT {

    private static final String PREFIX = "SearchOrgaIT";
    private static final String BEARER = "Bearer ";
    private final String jwtHeader = BEARER + getValidJwt();

    /**
     * Warning : The following tests are order dependent
     * Be careful when you are updating one of them
     */

    @Test
    @Order(0)
    void createOrganizationsForSearchApi() {
        String firstOrganization = JSONMaker.makeOrganization(PREFIX + "-SN-A", PREFIX + "-name-alpha");
        String secondOrganization = JSONMaker.makeOrganization(PREFIX + "-SN-B", PREFIX + "-name-beta");

        given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .body(secondOrganization)
                .when().post(ORGANIZATION_REST_PATH)
                .then().statusCode(201).extract();

        given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .body(firstOrganization)
                .when().post(ORGANIZATION_REST_PATH)
                .then().statusCode(201).extract();
    }

    @Test
    @Order(1)
    void searchApiGetIndexes() {
        given()
                .when().get(ORGANIZATION_REST_PATH + "/indexes")
                .then()
                .statusCode(200)
                .body(containsString("shortname"))
                .body(containsString("name"))
                .body(containsString("delegated"));
    }

    @Test
    @Order(1)
    void searchApiFilterByName() {
        given()
                .when().get(ORGANIZATION_REST_PATH + "?name=" + PREFIX + "-name-alpha")
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_RANGE, containsString("/"))
                .body("size()", equalTo(1))
                .body(containsString(PREFIX + "-SN-A"))
                .body(containsString("delegated"));
    }

    @Test
    @Order(1)
    void searchApiFilterByShortname() {
        given()
                .when().get(ORGANIZATION_REST_PATH + "?shortname=" + PREFIX + "-SN-A")
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_RANGE, containsString("Organization"))
                .body("size()", equalTo(1))
                .body(containsString(PREFIX + "-name-alpha"));
    }

    @Test
    @Order(1)
    void searchApiPagination() {
        given()
                .when().get(ORGANIZATION_REST_PATH + "?name=" + PREFIX + "-name&_offset=0&_limit=1")
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_RANGE, containsString("/2"))
                .body("size()", equalTo(1));
    }

    @Test
    @Order(1)
    void searchApiOrder() {
        given()
                .when().get(ORGANIZATION_REST_PATH + "?_sort=-name&name=" + PREFIX)
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_RANGE, containsString("/2"))
                .body("size()", equalTo(2))
                .body("name[0]", equalTo(PREFIX + "-name-beta"))
                .body("name[1]", equalTo(PREFIX + "-name-alpha"));

        given()
                .when().get(ORGANIZATION_REST_PATH + "?_sort=name&name=" + PREFIX)
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_RANGE, containsString("/2"))
                .body("size()", equalTo(2))
                .body("name[0]", equalTo(PREFIX + "-name-alpha"))
                .body("name[1]", equalTo(PREFIX + "-name-beta"));

        given()
                .when().get(ORGANIZATION_REST_PATH + "?_offset=0&name=" + PREFIX)
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("name[0]", equalTo(PREFIX + "-name-alpha"))
                .body("name[1]", equalTo(PREFIX + "-name-beta"));
    }

    @Test
    @Order(1)
    void searchApiDelegatedFilter() {
        given()
                .when().get(ORGANIZATION_REST_PATH + "?name=" + PREFIX + "-name&delegated=false")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));

        given()
                .when().get(ORGANIZATION_REST_PATH + "?name=" + PREFIX + "-name&delegated=true")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @Order(1)
    void searchApiPossiblesValuesFilter() {
        given()
                .when()
                .get(ORGANIZATION_REST_PATH + "/indexes/name/values?name="+PREFIX + "&_sort=lastUpdateTimestamp")
                .then()
                .statusCode(200)
                .body(containsString("[\"" + PREFIX + "-name-alpha\",\"" + PREFIX + "-name-beta\"]"))
                .body("size()", equalTo(2));

        given()
                .when()
                .get(ORGANIZATION_REST_PATH + "/indexes/shortname/values?shortname="+PREFIX)
                .then()
                .statusCode(200)
                .body(containsString("[\"" + PREFIX + "-SN-A\",\"" + PREFIX + "-SN-B\"]"))
                .body("size()", equalTo(2));

        given()
                .when().get(ORGANIZATION_REST_PATH + "/indexes/delegated/values")
                .then()
                .statusCode(200)
                .body(containsString("[\"true\",\"false\"]"))
                .body("size()", equalTo(2));

        given()
                .when().get(ORGANIZATION_REST_PATH + "/indexes/archived/values")
                .then()
                .statusCode(200)
                .body(containsString("[\"true\",\"false\"]"))
                .body("size()", equalTo(2));
    }

    @Test
    @Order(1)
    void searchApiDefaultSearchFilter() {
        given()
                .when().get(ORGANIZATION_REST_PATH + "?name=" + PREFIX + "-name&search=nonExistingOrga")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));

        given()
                .when().get(ORGANIZATION_REST_PATH + "?name=" + PREFIX + "-name&search=alpha")
                .then()
                .statusCode(200)
                .body("name[0]", equalTo(PREFIX + "-name-alpha"))
                .body("size()", equalTo(1));
    }

    @Test
    @Order(2)
    void testArchivedFilter() {
        String thirdOrganization = JSONMaker.makeOrganization(PREFIX + "-SN-C", PREFIX + "-name-charly");
        ExtractableResponse<Response> extractableResponse = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, jwtHeader)
                .body(thirdOrganization)
                .when().post(ORGANIZATION_REST_PATH)
                .then().statusCode(201).extract();

        String orgaId = extractableResponse.body().jsonPath().getString("id");

        String jwt = getValidJwt();
        given().when().header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .delete(ORGANIZATION_REST_PATH + "/" + orgaId)
                .then().statusCode(200);

        ExtractableResponse<Response> extractableResponse2 = given().when()
                .get(ORGANIZATION_REST_PATH + "?archived=true&name=" + PREFIX)
                .then().statusCode(200).extract();

        String name = extractableResponse2.body().jsonPath().getString("[0].name");
        assertEquals(PREFIX + "-name-charly", name);
        Boolean archived = extractableResponse2.body().jsonPath().getBoolean("[0].archived");
        assertEquals(true, archived);
    }
}

