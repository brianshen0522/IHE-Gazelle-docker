package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsMissingValidationReportResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsMissingValidationReportResource.class, restrictToAnnotatedClass = true)
class EvsValidationMissingReportInTestReportIT extends AbstractEvsValidationErrorIT {

    @Test
    void createValidationReturnsBadRequestWhenNoValidationReportIsProduced() {
        given()
              .header("Authorization", "Bearer " + validJwt())
              .contentType("application/json")
              .body(validationRequest("mock-validation-service"))
              .when()
              .post("/evs/rest/validations")
              .then()
              .statusCode(400)
              .body(is("Missing validation report identifier"));
    }
}
