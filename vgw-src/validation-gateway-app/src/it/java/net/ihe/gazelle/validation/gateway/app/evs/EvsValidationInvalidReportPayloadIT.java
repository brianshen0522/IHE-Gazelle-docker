package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsInvalidValidationReportResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsInvalidValidationReportResource.class, restrictToAnnotatedClass = true)
class EvsValidationInvalidReportPayloadIT extends AbstractEvsValidationErrorIT {

    @Test
    void getValidationReturnsServerErrorWhenValidationReportPayloadIsInvalid() {
        String jwt = validJwt();
        String validationLocation = createValidationAndGetLocation(jwt, "mock-validation-service");

        given()
              .header("Authorization", "Bearer " + jwt)
              .header("Accept", "application/json")
              .when()
              .get(validationLocation)
              .then()
              .statusCode(500)
              .body(is("Unexpected error while reading validation."));
    }
}
