package net.ihe.gazelle.validation.gateway.app.gateway;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.Header;
import jakarta.ws.rs.core.HttpHeaders;
import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

//@QuarkusIntegrationTest shows instability in CI, using @QuarkusTest for now
@QuarkusTest
@TestProfile(GatewayJwtTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = GatewayE2ETestResource.class, restrictToAnnotatedClass = true)
class GatewayE2EIT {

    @Test
    void getProfilesThroughGateway() {
        given()
              .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + OIDCJWTGenerator.getValidJwtWithGroups(
                      java.util.List.of("role:sut_operator"))))
              .when()
              .get("/rest/v1/profiles")
              .then()
              .statusCode(200)
              .header("Content-Range", is("profiles 1-7/7"))
              .body("$", hasSize(7))
              .body("validationService", hasItem("mock-validation-service"))
              .body("profile.profileID", hasItem("ITI-18_request"));
    }
}
