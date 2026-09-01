package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class GlobalEndpointIT {

    @Test
    void indexFileTest() {
        given()
                .when().get("/")
                .then()
                .statusCode(200)
                .body(containsString("Gazelle User Management"));
    }

    @Test
    void healthcheckEndpointTest() {
        given()
                .when().get("/health")
                .then()
                .statusCode(200)
                .body(containsString("\"status\": \"UP\""));
    }

    @Test
    void configurationsEndpointTest() {
        given()
                .when()
                .get("/rest/configurations")
                .then()
                .statusCode(200)
                .body(containsString("\"userRegistrationEnabled\":true"))
                .body(containsString("\"organizationCreationEnabled\":true"))
                .body(containsString("\"termsOfServiceUrl\":\"https://www.ihe-europe.net/privacy-policy\""));
    }
}
