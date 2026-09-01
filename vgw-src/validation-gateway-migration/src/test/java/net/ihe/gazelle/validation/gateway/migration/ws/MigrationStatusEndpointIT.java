package net.ihe.gazelle.validation.gateway.migration.ws;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestProfile(MongoTargetUnavailableProfile.class)
class MigrationStatusEndpointIT {

   @Test
   void rootPathWithoutTrailingSlashShouldRedirect() {
      given()
            .redirects().follow(false)
            .when()
            .get("")
            .then()
            .statusCode(308)
            .header("Location", endsWith("/evs-migration/"));
   }

   @Test
   void statusEndpointShouldRespondWithoutClasspathError() {
      given()
            .when()
            .get("/api/migration/status")
            .then()
            .statusCode(200)
            .body("state", notNullValue())
            .body("evsDatabaseAccessible", notNullValue())
            .body("evsDatabaseMessage", notNullValue())
            .body("targetType", not(isEmptyOrNullString()))
            .body("targetAccessible", notNullValue())
            .body("targetMessage", notNullValue());
   }

}
