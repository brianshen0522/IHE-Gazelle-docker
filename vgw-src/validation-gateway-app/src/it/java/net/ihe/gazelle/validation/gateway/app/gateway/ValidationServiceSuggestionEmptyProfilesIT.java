package net.ihe.gazelle.validation.gateway.app.gateway;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.Header;
import jakarta.ws.rs.core.HttpHeaders;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(GatewayJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
class ValidationServiceSuggestionEmptyProfilesIT {

    static Stream<Arguments> providesAuthorizedJWTs() {
        return Stream.of(
              Arguments.of(OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:user"))),
              Arguments.of(OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:sut_operator")))
        );
    }

    @ParameterizedTest
    @MethodSource("providesAuthorizedJWTs")
    void searchByValidationServiceNameReturnsProfilesFromThatServiceOnly(String jwt) {
        given()
              .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
              .when()
              .get("/rest/v1/profiles?validationService=mock-validation-service")
              .then()
              .statusCode(200)
              .body("$", hasSize(6))
              .body("validationService", everyItem(is("mock-validation-service")));
    }

    @ParameterizedTest
    @MethodSource("providesAuthorizedJWTs")
    void searchByEmptyProfilesServiceReturnsNoProfiles_reproducer(String jwt) {
        given()
              .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
              .when()
              .get("/rest/v1/profiles?validationService=mock-empty-profiles-service")
              .then()
              .statusCode(200)
              .body("$", hasSize(0));
    }

    @ParameterizedTest
    @MethodSource("providesAuthorizedJWTs")
    void possibleValuesForValidationServiceIncludesServiceWithNoProfiles(String jwt) {
        given()
              .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
              .when()
              .get("/rest/v1/profiles/indexes/validationService/values")
              .then()
              .statusCode(200)
              .body("$", hasItem("mock-validation-service"))
              .body("$", hasItem("mock-empty-profiles-service"));
    }
}
