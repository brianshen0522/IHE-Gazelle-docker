package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsMaestroDownResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(EvsJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsMaestroDownResource.class, restrictToAnnotatedClass = true)
class EvsValidationMaestroUnavailableIT extends AbstractEvsValidationErrorIT {

    @Test
    void createValidationReturnsServerErrorWhenMaestroIsUnavailable() {
        given()
              .header("Authorization", "Bearer " + validJwt())
              .contentType("application/json")
              .body(validationRequest("mock-validation-service"))
              .when()
              .post("/evs/rest/validations")
              .then()
              .statusCode(500)
              .body(is("Unexpected error while creating validation."));
    }
}
