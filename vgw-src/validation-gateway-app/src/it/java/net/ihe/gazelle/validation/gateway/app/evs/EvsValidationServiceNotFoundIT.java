package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsServiceNotFoundResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsServiceNotFoundResource.class, restrictToAnnotatedClass = true)
class EvsValidationServiceNotFoundIT extends AbstractEvsValidationErrorIT {

    @Test
    void createValidationReturnsBadRequestWhenServiceIsNotFound() {
        given()
              .header("Authorization", "Bearer " + validJwt())
              .contentType("application/json")
              .body(validationRequest("unknown-validation-service"))
              .when()
              .post("/evs/rest/validations")
              .then()
              .statusCode(400)
              .body(containsString("Validation service not found"));
    }
}
