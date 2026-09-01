package net.ihe.gazelle.validation.gateway.app.gateway;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.Header;
import jakarta.ws.rs.core.HttpHeaders;
import net.ihe.gazelle.validation.gateway.app.itmock.MockValidationServiceResource;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import net.ihe.gazelle.validation.gateway.quarkus.ws.ValidationProfileWithServiceDTO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(GatewayJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(MockValidationServiceResource.class)
class GetProfilesIT {

    static Stream<Arguments> providesAuthorizedJWTs() {
        return Stream.of(
                Arguments.of(OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:user"))),
                Arguments.of(OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:sut_operator")))
        );
    }

    @ParameterizedTest
    @MethodSource("providesAuthorizedJWTs")
    void getProfilesReturnsProfilesFromValidationService(String jwt) throws Exception {
        String body = given()
                .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .when()
                .get("/rest/v1/profiles")
                .then()
                .statusCode(200)
                .header("Content-Range", is("profiles 1-7/7"))
                .body("$", hasSize(7))
                .body("validationService", hasItem("mock-validation-service"))
                .body("profile.profileID", hasItem("ITI-18_request"))
                .body("profile.profileName", hasItem("ITI-18 Registry Stored Query Request"))
                .body("profile.domain", hasItem("ITI"))
                .body("profile.coveredItems.flatten()", hasItem("ITI-18 request A"))
                .body("validationService", hasItem("mock-model-based-service"))
                .body("profile.profileID", hasItem("MBV-VALIDATOR-1"))
                .extract()
                .asString();

        ObjectMapper mapper = new ObjectMapper();
        ValidationProfileWithServiceDTO[] parsed = mapper.readValue(body, ValidationProfileWithServiceDTO[].class);
        assertEquals(7, parsed.length);
    }

    @ParameterizedTest
    @MethodSource("providesAuthorizedJWTs")
    void readProfileReturnsSingleProfile(String jwt) {
        given()
                .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .when()
                .get("/rest/v1/profiles/mock-validation-service/ITI-18_request")
                .then()
                .statusCode(200)
                .body("profileID", is("ITI-18_request"))
                .body("profileName", is("ITI-18 Registry Stored Query Request"))
                .body("domain", is("ITI"))
                .body("coveredItems", hasItem("ITI-18 request A"));
    }

    @ParameterizedTest
    @MethodSource("providesAuthorizedJWTs")
    void searchProfilesSupportsPresentationFields(String jwt) {
        given()
                .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .when()
                .get("/rest/v1/profiles?_fields=profile.profileID")
                .then()
                .statusCode(200)
                .body("find { it.profile.profileID == 'ITI-18_request' }.profile.profileID", is("ITI-18_request"))
                .body("find { it.profile.profileID == 'ITI-18_request' }.profile.profileName", nullValue())
                .body("find { it.profile.profileID == 'ITI-18_request' }.profile.domain", nullValue());
    }

    @ParameterizedTest
    @MethodSource("providesAuthorizedJWTs")
    void readProfileSupportsPresentationFields(String jwt) {
        given()
                .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .when()
                .get("/rest/v1/profiles/mock-validation-service/ITI-18_request?_fields=profileID")
                .then()
                .statusCode(200)
                .body("profileID", is("ITI-18_request"))
                .body("profileName", nullValue())
                .body("domain", nullValue());
    }

    @org.junit.jupiter.api.Test
    void getProfilesReturnsUnauthorizedWithoutToken() {
        given()
                .when()
                .get("/rest/v1/profiles")
                .then()
                .statusCode(401);
    }

    @org.junit.jupiter.api.Test
    void getProfilesReturnsForbiddenWithoutRequiredRole() {
        String jwt = OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:unknown"));
        given()
                .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .when()
                .get("/rest/v1/profiles")
                .then()
                .statusCode(403);
    }
}
