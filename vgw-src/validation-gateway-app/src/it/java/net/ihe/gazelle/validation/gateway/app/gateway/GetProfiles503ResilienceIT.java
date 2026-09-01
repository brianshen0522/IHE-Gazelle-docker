package net.ihe.gazelle.validation.gateway.app.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.Header;
import jakarta.ws.rs.core.HttpHeaders;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import net.ihe.gazelle.validation.gateway.quarkus.ws.ValidationProfileWithServiceDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestProfile(GatewayProfiles503ResilienceTestProfile.class)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(value = GatewayProfiles503ResilienceTestResource.class, restrictToAnnotatedClass = true)
class GetProfiles503ResilienceIT {

   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

   @Test
   void getProfilesKeepsCachedProfilesAndSkipsRepeatedFailuresDuringCooldown() throws Exception {
      String jwt = OIDCJWTGenerator.getValidJwtWithGroups(List.of("role:sut_operator"));

      ValidationProfileWithServiceDTO[] warmup = getProfiles(jwt);
      assertThat(warmup.length, is(7));

      Thread.sleep(2500);
      GatewayProfiles503ResilienceTestResource.configureDelayed503ProfilesResponse(1200);
      GatewayProfiles503ResilienceTestResource.resetRequestJournal();

      ValidationProfileWithServiceDTO[] secondCall = getProfiles(jwt);
      assertThat(secondCall.length, is(7));
      assertThat(List.of(secondCall).stream().map(ValidationProfileWithServiceDTO::validationService).toList()
            .contains("mock-validation-service"), is(true));
      int profileRequestsAfterSecondCall = GatewayProfiles503ResilienceTestResource.countProfileRequests();

      ValidationProfileWithServiceDTO[] thirdCall = getProfiles(jwt);
      assertThat(thirdCall.length, is(7));
      assertThat(List.of(thirdCall).stream().map(ValidationProfileWithServiceDTO::validationService).toList()
            .contains("mock-validation-service"), is(true));
      assertThat(GatewayProfiles503ResilienceTestResource.countProfileRequests(), is(profileRequestsAfterSecondCall));
   }

   private ValidationProfileWithServiceDTO[] getProfiles(String jwt) throws Exception {
      String body = given()
            .header(new Header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
            .when()
            .get("/rest/v1/profiles")
            .then()
            .statusCode(200)
            .extract()
            .asString();
      return OBJECT_MAPPER.readValue(body, ValidationProfileWithServiceDTO[].class);
   }
}
