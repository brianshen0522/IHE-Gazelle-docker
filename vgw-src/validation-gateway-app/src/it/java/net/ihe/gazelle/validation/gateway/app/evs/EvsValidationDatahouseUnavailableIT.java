package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsDatahouseDownResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsDatahouseDownResource.class, restrictToAnnotatedClass = true)
class EvsValidationDatahouseUnavailableIT extends AbstractEvsValidationErrorIT {

    @Test
    void getValidationReturnsServerErrorWhenDatahouseIsUnavailable() {
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
