package net.ihe.gazelle.validation.gateway.app.evs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.validation.gateway.app.itmock.MockEvsServiceRegistryDownResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(EvsServiceRegistryDownJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = MockEvsServiceRegistryDownResource.class, restrictToAnnotatedClass = true)
class EvsValidationProfilesServiceRegistryDownIT extends AbstractEvsValidationErrorIT {

    @Test
    void getProfilesReturnsServerErrorWhenServiceRegistryIsDown() {
        given()
              .header("Authorization", "Bearer " + validJwt())
              .header("Accept", "application/json")
              .when()
              .get("/evs/rest/validations/profiles")
              .then()
              .statusCode(500)
              .body(is("Unexpected error while listing profiles."));
    }
}
