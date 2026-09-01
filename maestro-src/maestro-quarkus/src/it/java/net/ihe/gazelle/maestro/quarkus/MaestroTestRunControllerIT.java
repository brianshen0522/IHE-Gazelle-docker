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

package net.ihe.gazelle.maestro.quarkus;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.maestro.api.business.testreport.Result;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testreport.validator.TestReportValidator;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import org.apache.http.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(TestRunControllerProfile.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KeycloakMockResource.class)
@QuarkusTestResource(MaestroExternalServicesResource.class)
class MaestroTestRunControllerIT {

   @ConfigProperty(name = "gzl.it.port")
   int port;

   TestReportValidator reportValidator = new TestReportValidator();
   String jwt;

   @BeforeAll
   void setUp() throws IOException {
      jwt = getValidJwt();
      WireMockSingleton.startServer(port);
      WireMockSingleton.mockCallback();
      WireMockSingleton.mockServiceRegistry();
      WireMockSingleton.mockGetProfiles();
      WireMockSingleton.mockEvsProfiles();
      WireMockSingleton.mockValidate();
      WireMockSingleton.mockKeycloak();
      MockDatahouseServer.start(34501);
   }

   @AfterAll
   void tearDown() {
      WireMockSingleton.stop();
      try {
         MockDatahouseServer.stop();
      } catch (IOException e) {
         // log and ignore in teardown
      }
   }

   @Test
   void synchronous_test_executeTestSuite_with_one_step() throws IOException {
      String testRun = WireMockSingleton.getResourceAsString("/rest/testSuiteRun_ok.json");
      Response response = given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .body(testRun)
            .when().post("/v1/test-suite/run")
            .then()
            .extract().response();
      assertNotNull(response);
      TestReport testReport = response.as(TestReportDTO.class).getBusinessObject();
      reportValidator.assertValid(testReport);
      assertEquals(Result.PASSED, testReport.getResult());
   }

   @Test
   void asynchronous_test_executeTestSuite_with_one_step() throws IOException {
      String testRun = WireMockSingleton.getResourceAsString("/rest/testSuiteRun_ok.json");
      String callback = "http://localhost:" + port + "/mock/gazelle/rest";
      given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .body(testRun)
            .when().post("/v1/test-suite/run?callback=" + callback)
            .then()
            .statusCode(202);

      TestReport testReport = WireMockSingleton.awaitTestReport();
      reportValidator.assertValid(testReport);
      assertEquals(Result.PASSED, testReport.getResult());
   }

   @Test
   void synchronous_test_executeTestRun_with_one_step() throws IOException {
      String testRun = WireMockSingleton.getResourceAsString("/rest/testRun_ok.json");
      Response response = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .body(testRun)
            .when().post("/v1/test/run")
            .then()
            .extract().response();
      assertNotNull(response);
      TestReport testReport = response.as(TestReportDTO.class).getBusinessObject();
      reportValidator.assertValid(testReport);
      assertEquals(Result.PASSED, testReport.getResult());
   }

   @Test
   void asynchronous_test_executeTestRun_with_one_step() throws IOException {
      String testRun = WireMockSingleton.getResourceAsString("/rest/testRun_ok.json");
      String callback = "http://localhost:" + port + "/mock/gazelle/rest";
      given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .body(testRun)
            .when().post("/v1/test/run?callback=" + callback)
            .then()
            .statusCode(202);

      TestReport testReport = WireMockSingleton.awaitTestReport();
      reportValidator.assertValid(testReport);
      assertEquals(Result.PASSED, testReport.getResult());
   }

   @Test
   void shall_return_undefined_invalid_step_type() throws IOException {
      String testRun = WireMockSingleton.getResourceAsString("/rest/invalid_step_type.json");
      Response response = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .body(testRun)
            .when().post("/v1/test-suite/run")
            .then()
            .extract().response();
      assertNotNull(response);
      TestReport testReport = response.as(TestReportDTO.class).getBusinessObject();
      reportValidator.assertValid(testReport);
      assertEquals(Result.UNDEFINED, testReport.getResult());
   }

   @Test
   void shall_return_undefined_invalid_property_name() throws IOException {
      String testRun = WireMockSingleton.getResourceAsString("/rest/invalid_property_name.json");
      Response response = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .body(testRun)
            .when().post("/v1/test-suite/run")
            .then()
            .extract().response();
      assertNotNull(response);
      TestReport testReport = response.as(TestReportDTO.class).getBusinessObject();
      reportValidator.assertValid(testReport);
      assertEquals(Result.UNDEFINED, testReport.getResult());
   }

   @Test
   void synchronous_test_executeTestRun_with_user_interaction_unsupported() throws IOException {
      String testRun = WireMockSingleton.getResourceAsString("/rest/user_interaction.json");
      Response response = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .body(testRun)
            .when().post("/v1/test/run")
            .then()
            .extract().response();
      assertNotNull(response);
      TestReport testReport = response.as(TestReportDTO.class).getBusinessObject();
      reportValidator.assertValid(testReport);
      assertEquals(Result.UNDEFINED, testReport.getResult());
      String error = testReport.getTestRunReports().getFirst()
            .getStepRunReports().getFirst()
            .getUnexpectedErrors().getFirst().getMessage();
      assertTrue(error.contains("User interaction is not supported via REST API."));
   }

   @ParameterizedTest
   @MethodSource("invalidTestExecuteTestSuiteProvider")
   void should_fail_deserialization(String filePath, String expectedMessage) throws IOException {
      String testRun = WireMockSingleton.getResourceAsString(filePath);
      String response = given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .body(testRun)
            .when().post("/v1/test-suite/run")
            .then()
            .statusCode(400)
            .extract().response()
            .body().asString();

      assertThat(response, containsString(expectedMessage));
   }

   private static Stream<Arguments> invalidTestExecuteTestSuiteProvider() {
      return Stream.of(
            Arguments.of("/rest/invalid_property_type.json", "Could not resolve type id"),
            Arguments.of("/rest/invalid_structure.json", "=> invalid")
      );
   }
}
