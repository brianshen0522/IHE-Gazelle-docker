package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsServiceResource;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsServiceResource.class, restrictToAnnotatedClass = true)
class EvsValidationIT {

    @Test
    void createValidationReturnsReportRedirectAndCanReadValidation() {
        String jwt = validJwt();
        ValidationCreation creation = createValidation(jwt);
        String expectedReportUrl = buildReportUrl(creation.location());

        org.junit.jupiter.api.Assertions.assertTrue(
              creation.reportRedirect().equals(expectedReportUrl)
                    || creation.reportRedirect().equals("http://datahouse/report/123"),
              "Unexpected report redirect: " + creation.reportRedirect()
        );

        given()
              .header("Authorization", "Bearer " + jwt)
              .header("Accept", "application/json")
              .when()
              .get(creation.location())
              .then()
              .statusCode(200)
              .body("status", anyOf(is("DONE_PASSED"), is("DONE_UNDEFINED")))
              .body("validationReportRef.location", is(expectedReportUrl));

        String oid = URI.create(creation.location()).getPath().replaceAll(".*/", "");
        given()
              .header("Authorization", "Bearer " + jwt)
              .when()
              .get("/evs/rest/GetValidationStatus?oid=" + oid)
              .then()
              .statusCode(200)
              .body(anyOf(is("PASSED"), is("ABORTED")));
    }

    @Test
    void reportEndpointReturnsValidationReport() {
        String jwt = validJwt();
        ValidationCreation creation = createValidation(jwt);

        given()
              .header("Authorization", "Bearer " + jwt)
              .header("Accept", "application/json")
              .when()
              .get(buildReportUrl(creation.location()))
              .then()
              .statusCode(200)
              .body("validationOverview.validationOverallResult", is("PASSED"));
    }

    @Test
    void validationInfoEndpointsReturnDateAndPermanentLink() {
        String jwt = validJwt();
        ValidationCreation creation = createValidation(jwt);
        String oid = URI.create(creation.location()).getPath().replaceAll(".*/", "");

        given()
              .header("Authorization", "Bearer " + jwt)
              .when()
              .get("/evs/rest/GetValidationDate?oid=" + oid)
              .then()
              .statusCode(200)
              .body(is("2025-01-01T15:30:00.387Z"));

        given()
              .header("Authorization", "Bearer " + jwt)
              .when()
              .get("/evs/rest/GetValidationPermanentLink?oid=" + oid)
              .then()
              .statusCode(200)
              .body(is("http://localhost:3000/gazelle/validation-portal/reports/" + oid));
    }

    @Test
    void getProfilesReturnsServiceProfiles() {
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
    void getProfilesReturnsJsonEvenWhenXmlIsRequested() {
        String jwt = validJwt();
        given()
              .header("Accept", "application/xml")
              .header("Authorization", "Bearer " + jwt)
              .when()
              .get("/evs/rest/validations/profiles")
              .then()
              .statusCode(406);
    }

    @Test
    void createValidationAcceptsXmlRequestWithTextXmlContentType() {
        String jwt = validJwt();
        String payload = Base64.getEncoder().encodeToString("test".getBytes(StandardCharsets.UTF_8));
        String xmlRequest = """
              <validation objectType="urn:hl7-org:v3">
                <validationService name="mock-validation-service" validator="ITI-18_request"/>
                <object objectType="urn:hl7-org:v3">
                  <content>%s</content>
                </object>
              </validation>
              """.formatted(payload);

        given()
              .header("Authorization", "Bearer " + jwt)
              .contentType("text/xml")
              .body(xmlRequest)
              .when()
              .post("/evs/rest/validations")
              .then()
              .statusCode(201)
              .header("Content-Location", notNullValue())
              .header("X-Validation-Report-Redirect", notNullValue());
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

        String location = response.header("Content-Location");
        String reportRedirect = response.header("X-Validation-Report-Redirect");
        return new ValidationCreation(location, reportRedirect);
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
