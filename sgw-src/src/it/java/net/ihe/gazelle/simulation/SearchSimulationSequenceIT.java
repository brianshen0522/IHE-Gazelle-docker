/*
 * Copyright 2025-2026 IHE International.
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

package net.ihe.gazelle.simulation;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import net.ihe.gazelle.search.api.IndexedField;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.simulation.technical.dto.ResolvedSimulationSequenceDTO;
import org.apache.http.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KeycloakMockResource.class)
class SearchSimulationSequenceIT {

   @ConfigProperty(name = "gzl.it-port")
   int port;

   String jwt;

   @BeforeAll
   void setup() throws IOException {
      jwt = getValidJwt();
      WireMockSingleton.startServer(port);
      WireMockSingleton.mockSimulationSequence();
      WireMockSingleton.mockServiceRegistry();
      WireMockSingleton.mockChecksum();
   }

   @Test
   void should_get_indexes() {
      List<IndexedField> indexes = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences/indexes")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(9, indexes.size());
   }

   @Test
   void should_get_possible_values_for_field_id() {
      List<String> possibleValues = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences/indexes/id/values")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(3, possibleValues.size());
      assertTrue(possibleValues.contains("CHXDS_ITI-41-42"));
   }

   @Test
   void should_not_found_for_unknown_field() {
      given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences/indexes/unknown/values")
            .then()
            .statusCode(404);
   }

   @Test
   void search_should_return_all_sequences() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(3, sequences.size());
   }

   @Test
   void search_should_return_filter_by_service_name() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?serviceName=XDS")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(3, sequences.size());
   }

   @Test
   void search_should_return_filter_by_id() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?id=CHXDS_ITI-41-42")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(1, sequences.size());
   }

   @Test
   void search_should_return_filter_by_simulated_role() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?simulatedRole=Document Repository")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(1, sequences.size());
   }

   @Test
   void search_should_return_filter_by_tested_role() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?testedRole=Document Registry")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(1, sequences.size());
   }

   @Test
   void search_should_return_filter_by_transaction() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?transactions=ITI-43")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(1, sequences.size());
   }

   @Test
   void search_should_return_filter_by_standard() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?standards=ebRim")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(2, sequences.size());
   }

   @Test
   void search_should_return_filter_by_short_description() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?shortDescription=XDS Document Source")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(1, sequences.size());
   }

   @Test
   void search_should_return_filter_by_runnability() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?runnable=true")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(1, sequences.size());
      assertTrue(sequences.getFirst().getRunnable());
   }

   @Test
   void search_should_return_filter_by_validity() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?valid=true")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(2, sequences.size());
      assertTrue(sequences.getFirst().getValid());
   }

   @Test
   void search_should_return_filter_by_invalidity() {
      List<ResolvedSimulationSequenceDTO> sequences = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences?valid=false")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(new TypeRef<>() {
            });

      assertEquals(1, sequences.size());
      assertFalse(sequences.getFirst().getValid());
   }

   @Test
   void should_get_sequence_by_id() {
      ResolvedSimulationSequenceDTO sequence = given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences/CHXDS_ITI-41-42")
            .then()
            .statusCode(200)
            .extract()
            .response()
            .as(ResolvedSimulationSequenceDTO.class);

      assertEquals("CHXDS_ITI-41-42", sequence.getId());
      assertFalse(sequence.getRunnable());
   }

   @Test
   void should_bad_request_when_no_existing_id() {
      given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .when()
            .get("/rest/simulation/v1/sequences/unknown")
            .then()
            .statusCode(404);
   }

   static Stream<Arguments> provideEndpoints() {
      return Stream.of(
            Arguments.of("/rest/simulation/v1/sequences/CHXDS_ITI-41-42"),
            Arguments.of("/rest/simulation/v1/sequences/indexes"),
            Arguments.of("/rest/simulation/v1/sequences/indexes/id/values"),
            Arguments.of("/rest/simulation/v1/sequences")
      );
   }

   @ParameterizedTest()
   @MethodSource("provideEndpoints")
   void should_unauthorized_when_no_token(String endpoint) {
      given()
            .accept(ContentType.JSON)
            .when()
            .get(endpoint)
            .then()
            .statusCode(401);
   }
}
