/*
 * Copyright 2026 IHE International.
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

import net.ihe.gazelle.itb.gateway.integration.ItbWireMockStubs;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testreport.Result;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.technical.dto.report.TestReportDTO;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import org.apache.http.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.security.mocks.OIDCJWTGenerator.getValidJwt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@TestProfile(TestRunControllerProfile.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KeycloakMockResource.class)
class ItbStepControllerIT {

    private static final String ITB_SESSION_ID = "SYNC-SESSION-IT";
    private static final String ITB_REPORT = "<ItbReport><n1/><n2/><n3/><n4/><n5/><n6/><n7/><status>SUCCESS</status></ItbReport>";
    private static final byte[] PDF_CONTENT = "pdf-content".getBytes(StandardCharsets.UTF_8);

    @ConfigProperty(name = "gzl.it.port")
    int port;

    private String jwt;

    @BeforeAll
    void setUp() throws IOException {
        jwt = getValidJwt();
        WireMockSingleton.startServer(port);
        WireMockSingleton.mockCallback();
        WireMockSingleton.mockServiceRegistry();
        WireMockSingleton.mockKeycloak();
        MockDatahouseServer.start(34501);
    }

    @AfterAll
    void tearDown() {
        WireMockSingleton.stop();
        try {
            MockDatahouseServer.stop();
        } catch (IOException e) {
            // no-op in teardown
        }
    }

    @Test
    void synchronousRun_withItbStep_shouldReturnPassedReportAndItbOutputs() throws IOException {
        ItbWireMockStubs.stubStartSyncSuccess(WireMockSingleton.getWireMockServer(), ITB_SESSION_ID);
        ItbWireMockStubs.stubStatusReportAndLogs(WireMockSingleton.getWireMockServer(), ITB_SESSION_ID, ITB_REPORT, "line1", "line2");
        ItbWireMockStubs.stubPdfReportSuccess(WireMockSingleton.getWireMockServer(), ITB_SESSION_ID, PDF_CONTENT);

        String testRun = WireMockSingleton.getResourceAsString("/rest/testRun_itb_sync.json");
        Response response = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .body(testRun)
                .when().post("/v1/test/run")
                .then()
                .extract().response();

        assertNotNull(response);
        TestReport report = response.as(TestReportDTO.class).getBusinessObject();
        assertEquals(Result.PASSED, report.getResult());

        var stepReport = report.getTestRunReports().getFirst().getStepRunReports().getFirst();
        assertEquals(StepResult.PASSED, stepReport.getResult());
        List<String> outputNames = stepReport.getOutputs().stream().map(Property::getName).toList();
        assertTrue(outputNames.contains("xmlReport"));
        assertTrue(outputNames.contains("pdfReport"));
        assertTrue(outputNames.contains("logs"));

        WireMockSingleton.getWireMockServer().verify(postRequestedFor(urlEqualTo("/api/rest/tests/start"))
                .withRequestBody(matchingJsonPath("$.waitForCompletion", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.maximumWaitTime")));
        WireMockSingleton.getWireMockServer().verify(postRequestedFor(urlEqualTo("/api/rest/tests/status"))
                .withRequestBody(matchingJsonPath("$.withReports", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.withLogs", equalTo("true"))));
    }

    @Test
    void asynchronousRun_withItbStep_shouldCompleteViaItbCallback() throws IOException {
        String asyncSessionId = "ASYNC-SESSION-IT";
        ItbWireMockStubs.stubStartSuccess(WireMockSingleton.getWireMockServer(), asyncSessionId);
        ItbWireMockStubs.stubStatusLogs(WireMockSingleton.getWireMockServer(), asyncSessionId, "async-log-line");
        ItbWireMockStubs.stubPdfReportSuccess(WireMockSingleton.getWireMockServer(), asyncSessionId, PDF_CONTENT);

        String callback = "http://localhost:" + port + "/mock/gazelle/rest";
        String testRun = WireMockSingleton.getResourceAsString("/rest/testRun_itb_async.json");
        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .body(testRun)
                .when().post("/v1/test/run?callback=" + callback)
                .then()
                .statusCode(202);

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                WireMockSingleton.getWireMockServer().verify(postRequestedFor(urlEqualTo("/api/rest/tests/start"))));

        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(buildItbCallbackPayload(asyncSessionId, "case-001", ITB_REPORT))
                .when().post("/itb/report")
                .then()
                .statusCode(200);

        TestReport report = WireMockSingleton.awaitTestReport();
        assertEquals(Result.PASSED, report.getResult());
        var stepReport = report.getTestRunReports().getFirst().getStepRunReports().getFirst();
        assertEquals(StepResult.PASSED, stepReport.getResult());
        List<String> outputNames = stepReport.getOutputs().stream().map(Property::getName).toList();
        assertTrue(outputNames.contains("xmlReport"));
        assertTrue(outputNames.contains("pdfReport"));
        assertTrue(outputNames.contains("logs"));

        WireMockSingleton.getWireMockServer().verify(postRequestedFor(urlEqualTo("/api/rest/tests/start")));
        WireMockSingleton.getWireMockServer().verify(postRequestedFor(urlEqualTo("/api/rest/tests/status"))
                .withRequestBody(matchingJsonPath("$.withLogs", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.withReports", equalTo("false"))));
    }

    private String buildItbCallbackPayload(String sessionId, String testCaseId, String reportXml) {
        return """
                {
                  "inputs": [
                    {
                      "name": "system",
                      "type": "map",
                      "item": [
                        {"name": "id", "type": "number", "value": "123"},
                        {"name": "shortName", "type": "string", "value": "TEST_SYS"},
                        {"name": "fullName", "type": "string", "value": "Test System"}
                      ]
                    },
                    {
                      "name": "testSession",
                      "type": "map",
                      "item": [
                        {"name": "testSuiteIdentifier", "type": "string", "value": "suite-001"},
                        {"name": "testCaseIdentifier", "type": "string", "value": "%s"},
                        {"name": "testSessionIdentifier", "type": "string", "value": "%s"}
                      ]
                    },
                    {
                      "name": "testReport",
                      "type": "string",
                      "value": "%s"
                    }
                  ]
                }
                """.formatted(testCaseId, sessionId, reportXml.replace("\"", "\\\""));
    }
}
