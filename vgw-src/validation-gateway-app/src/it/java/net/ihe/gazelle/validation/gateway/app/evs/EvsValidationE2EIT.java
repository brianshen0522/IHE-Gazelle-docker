package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = EvsE2ETestResource.class, restrictToAnnotatedClass = true)
class EvsValidationE2EIT {

    @Test
    void getProfilesFromEvsApi() {
        String jwt = validJwt();
        given()
              .header("Accept", "application/json")
              .header("Authorization", "Bearer " + jwt)
              .when()
              .get("/evs/rest/validations/profiles")
              .then()
              .statusCode(200)
              .body("$", hasSize(7))
              .body("serviceName", hasItem("mock-validation-service"))
              .body("validator.keyword", hasItem("ITI-18_request"))
              .body("validator.name", hasItem("ITI-18 Registry Stored Query Request"))
              .body("validator.domain", hasItem("ITI"));
    }

    @Test
    void createValidationPersistsReportAndCanReadBack() {
        String jwt = validJwt();
        ValidationCreation creation = createValidation(jwt);

        given()
              .header("Authorization", "Bearer " + jwt)
              .header("Accept", "application/json")
              .when()
              .get(creation.location())
              .then()
              .statusCode(200)
              .body("status", anyOf(is("DONE_PASSED"), is("DONE_UNDEFINED"), is("DONE_FAILED")))
              .body("validationReportRef.location", notNullValue());

        await().atMost(Duration.ofSeconds(30))
              .pollInterval(Duration.ofSeconds(2))
              .untilAsserted(() -> given()
                    .header("Authorization", "Bearer " + jwt)
                    .header("Accept", "application/json")
                    .when()
                    .get(buildReportUrl(creation.location()))
                    .then()
                    .statusCode(200)
                    .body("validationOverview.validationOverallResult", anyOf(is("PASSED"), is("FAILED"), is("UNDEFINED"))));
    }

    @Test
    void validationOidEndpointReturnsStatusAndDetails() {
        String jwt = validJwt();
        ValidationCreation creation = createValidation(jwt);
        String oid = extractOid(creation.location());

        given()
              .header("Authorization", "Bearer " + jwt)
              .header("Accept", "application/json")
              .when()
              .get("/evs/rest/validations/" + oid)
              .then()
              .statusCode(200)
              .body("oid", is(oid))
              .body("validationService.name", is("mock-validation-service"))
              .body("status", anyOf(is("DONE_PASSED"), is("DONE_UNDEFINED"), is("DONE_FAILED")));

        given()
              .header("Authorization", "Bearer " + jwt)
              .header("Accept", "application/xml")
              .when()
              .get("/evs/rest/validations/" + oid)
              .then()
              .statusCode(200)
              .contentType("application/xml");
    }

    @Test
    void reportEndpointCanReturnXml() {
        String jwt = validJwt();
        ValidationCreation creation = createValidation(jwt);

        given()
                .header("Authorization", "Bearer " + jwt)
                .header("Accept", "application/xml")
                .when()
                .get(buildReportUrl(creation.location()))
                .then()
                .statusCode(200)
                .contentType("application/xml")
                .body(containsString("<ValidationReport"));
    }

    private ValidationCreation createValidation(String jwt) {
        String payload = Base64.getEncoder().encodeToString("test".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> request = Map.of(
              "validationService", Map.of(
                    "name", "mock-validation-service",
                    "validator", "ITI-18_request"
              ),
              "object", List.of(Map.of(
                    "objectType", "urn:hl7-org:v3",
                    "content", payload
              ))
        );

        Response response = given()
              .config(config().httpClient(httpClientConfig()
                    .setParam("http.connection.timeout", 120000)
                    .setParam("http.socket.timeout", 120000)))
              .header("Authorization", "Bearer " + jwt)
              .contentType("application/json")
              .body(request)
              .when()
              .post("/evs/rest/validations")
              .then()
              .statusCode(201)
              .header("Content-Location", notNullValue())
              .header("X-Validation-Report-Redirect", notNullValue())
              .extract()
              .response();

        return new ValidationCreation(response.header("Content-Location"),
              response.header("X-Validation-Report-Redirect"));
    }

    private String extractOid(String location) {
        return URI.create(location).getPath().replaceAll(".*/", "");
    }

    private record ValidationCreation(String location, String reportRedirect) {
    }

    private String buildReportUrl(String location) {
        URI uri = URI.create(location);
        String path = uri.getPath();
        if (!path.endsWith("/report")) {
            path = path + "/report";
        }
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), path, uri.getQuery(), null).toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build report URL from " + location, e);
        }
    }

    private String validJwt() {
        return OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:sut_operator"));
    }
}
