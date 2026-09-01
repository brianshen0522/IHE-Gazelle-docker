package net.ihe.gazelle.xmlvalidation.ws;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.xmlvalidation.ws.config.WrongIntegrationConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
@QuarkusTestResource(value = WrongIntegrationConfig.class, restrictToAnnotatedClass = true)

class ProfilesErrorIT {

    String expectedErrorMessageId = "[{\"profileID\":\"valid_profile_without_schematron_path_profile\",\"profileName\":\"test_profile\",\"domain\":\"test_domain\"}]";

   @Test
   void getValidationProfilesTestShouldReturnOnly() { // valid profile without schematron path will not execute sechmatron validator
      String profiles = when()
            .get("/rest/validation/v2/profiles")
            .then()
            .statusCode(200)
            .extract()
            .body().asString();
        assertThat(profiles, Matchers.containsString(expectedErrorMessageId));

   }

}
