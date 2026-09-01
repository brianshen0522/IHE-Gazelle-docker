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

package net.ihe.gazelle.itb.gateway.integration;

import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInput;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInputMappingEntry;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStartRequest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItbHttpClientIT extends ItbWireMockITSupport {

    @Test
    void startTest_shouldSendExpectedPayloadAndReturnSessionId() {
        String expectedSessionId = "ITB-SESSION-001";
        stubStartSuccess(expectedSessionId);

        String createdSessionId = itbHttpClient.startTest(buildRequest());

        assertEquals(expectedSessionId, createdSessionId);
        wireMock.verify(postRequestedFor(urlEqualTo("/api/rest/tests/start"))
                .withHeader("ITB_API_KEY", equalTo(API_KEY))
                .withRequestBody(matchingJsonPath("$.system", equalTo("sys-001")))
                .withRequestBody(matchingJsonPath("$.actor", equalTo("actor-001")))
                .withRequestBody(matchingJsonPath("$.testSuite[0]", equalTo("suite-001")))
                .withRequestBody(matchingJsonPath("$.testCase[0]", equalTo("case-001")))
                .withRequestBody(matchingJsonPath("$.inputMapping[0].input.name", equalTo("freeText")))
                .withRequestBody(matchingJsonPath("$.inputMapping[1].testSuite[0]", equalTo("suite-001")))
                .withRequestBody(matchingJsonPath("$.inputMapping[2].testCase[0]", equalTo("case-001"))));
    }

    @Test
    void getTestLogs_shouldCallStatusEndpointWithWithLogsFlag() {
        stubStatusLogs("SESSION-A", "line1", "line2");

        String logs = itbHttpClient.getTestLogs("SESSION-A");

        assertEquals("line1\nline2", logs);
        wireMock.verify(postRequestedFor(urlEqualTo("/api/rest/tests/status"))
                .withHeader("ITB_API_KEY", equalTo(API_KEY))
                .withRequestBody(matchingJsonPath("$.session[0]", equalTo("SESSION-A")))
                .withRequestBody(matchingJsonPath("$.withLogs", equalTo("true"))));
    }

    @Test
    void requestPdfReport_shouldReturnBinaryContent() {
        byte[] expectedPdf = "pdf-bytes".getBytes(StandardCharsets.UTF_8);
        stubPdfReportSuccess("SESSION-PDF", expectedPdf);

        byte[] pdfReport = itbHttpClient.requestPDFReport("SESSION-PDF", "case-001");

        assertArrayEquals(expectedPdf, pdfReport);
    }

    @Test
    void startTestAndWait_shouldSendSyncFieldsAndReturnReporting() {
        String report = "<ItbReport><n1/><n2/><n3/><n4/><n5/><n6/><n7/><status>SUCCESS</status></ItbReport>";
        stubStartSyncSuccess("SYNC-SESSION-1");
        stubStatusReportAndLogs("SYNC-SESSION-1", report, "sync-log-line");

        ItbReporting itbReporting = itbHttpClient.startTestAndWait(buildRequest(), 60_000L);

        assertNotNull(itbReporting.getTestSession());
        assertEquals("SYNC-SESSION-1", itbReporting.getTestSession().getTestSessionIdentifier());
        assertEquals("case-001", itbReporting.getTestSession().getTestCaseIdentifier());
        assertEquals(report, itbReporting.getTestReport());
        assertEquals("sync-log-line", itbReporting.getLogs());
        wireMock.verify(postRequestedFor(urlEqualTo("/api/rest/tests/start"))
                .withRequestBody(matchingJsonPath("$.waitForCompletion", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.maximumWaitTime", equalTo("60000"))));
        wireMock.verify(postRequestedFor(urlEqualTo("/api/rest/tests/status"))
                .withRequestBody(matchingJsonPath("$.withLogs", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.withReports", equalTo("true"))));
    }

    private ItbStartRequest buildRequest() {
        return new ItbStartRequest()
                .setSystem("sys-001")
                .setActor("actor-001")
                .setTestSuite(List.of("suite-001"))
                .setTestCase(List.of("case-001"))
                .setInputMapping(List.of(
                        new ItbInputMappingEntry().setInput(new ItbInput()
                                .setName("freeText")
                                .setType("string")
                                .setValue("hello")),
                        new ItbInputMappingEntry()
                                .setTestSuite(List.of("suite-001"))
                                .setInput(new ItbInput()
                                        .setName("suiteInput")
                                        .setType("number")
                                        .setValue("7")),
                        new ItbInputMappingEntry()
                                .setTestCase(List.of("case-001"))
                                .setInput(new ItbInput()
                                        .setName("caseFile")
                                        .setType("binary")
                                        .setEmbeddingMethod("BASE64")
                                        .setValue("ZmFrZQ=="))
                ));
    }
}
