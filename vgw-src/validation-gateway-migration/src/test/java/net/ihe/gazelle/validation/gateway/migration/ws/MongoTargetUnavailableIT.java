package net.ihe.gazelle.validation.gateway.migration.ws;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestProfile(MongoTargetUnavailableProfile.class)
class MongoTargetUnavailableIT {

   @Test
   void statusShouldExposeMongoTargetAsUnavailable() {
      given()
            .when()
            .get("/api/migration/status")
            .then()
            .statusCode(200)
            .body("targetType", equalTo("mongo"))
            .body("targetAccessible", equalTo(false))
            .body("targetMessage", containsString("MongoDB target unreachable"));
   }

   @Test
   void startShouldReturnServiceUnavailableWhenMongoTargetIsDown() {
      given()
            .when()
            .post("/api/migration/start?freezeConfirmed=true")
            .then()
            .statusCode(503)
            .body(containsString("MongoDB target unreachable"));
   }
}
