/*
 * Copyright 2025 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.serviceregistry.technical.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KeycloakMockResource.class)
class ServiceRegistrationControllerTest {

   private final TextSerDes serDes = new JacksonSerDes();

   @Test
   void testGetServiceNotFound() {
      String instanceId = "abcdef";
      String replicaId = "001";

      given().log().ifValidationFails()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + OIDCJWTGenerator.getValidJwt())
            .header("Content-Type", "application/json")
            .body(serDes.serialize(new ServiceDTO<>(getTestService(instanceId, replicaId))))
            .put("/services/{instanceId}/{replicaId}", instanceId, replicaId)
            .then()
            .log().ifValidationFails()
            .assertThat()
            .statusCode(200)
            .contentType("application/json")
            .body("name", equalTo("Test Service"))
            .body("selfRegistered", equalTo(true))
            .body("status", equalTo("AVAILABLE"));
   }

   @Test
   void testBadRequest() {
      String instanceId = "abcdef";
      String replicaId = "001";

      given().log().ifValidationFails()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + OIDCJWTGenerator.getValidJwt())
            .header("Content-Type", "application/json")
            .body("{}")
            .put("/services/{instanceId}/{replicaId}", instanceId, replicaId)
            .then()
            .log().ifValidationFails()
            .assertThat()
            .statusCode(400)
            .contentType("text/plain")
            .body(containsString("Invalid service metadata: Service name must not be null or empty => invalid"));
   }

   @Test
   void testUnauthorized() {
      String instanceId = "abcdef";
      String replicaId = "001";

      given().log().ifValidationFails()
            .when()
            .header("Content-Type", "application/json")
            .body(serDes.serialize(new ServiceDTO<>(getTestService(instanceId, replicaId))))
            .put("/services/{instanceId}/{replicaId}", instanceId, replicaId)
            .then()
            .log().ifValidationFails()
            .assertThat()
            .statusCode(401)
            .contentType("text/plain")
            .body(containsString("Unauthorized"));
   }

   @Test
   void testForbidden() {
      String instanceId = "abcdef";
      String replicaId = "001";

      given().log().ifValidationFails()
            .when()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + OIDCJWTGenerator.getValidJwtWithGroups(List.of("some-other-group")))
            .header("Content-Type", "application/json")
            .body(serDes.serialize(new ServiceDTO<>(getTestService(instanceId, replicaId))))
            .put("/services/{instanceId}/{replicaId}", instanceId, replicaId)
            .then()
            .log().ifValidationFails()
            .assertThat()
            .statusCode(403)
            .contentType("text/plain")
            .body(containsString("Forbidden"));
   }

   private static Service getTestService(final String instanceId, final String replicaId) {
      return new ServiceBuilder()
            .setName("Test Service")
            .setVersion("1.0.0")
            .setInstanceId(instanceId)
            .setReplicaId(replicaId)
            .build();
   }
}