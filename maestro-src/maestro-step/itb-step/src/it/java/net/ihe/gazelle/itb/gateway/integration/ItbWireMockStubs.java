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

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Shared ITB WireMock stubs reused by integration tests across modules.
 */
public final class ItbWireMockStubs {

    private ItbWireMockStubs() {
        // utility class
    }

    public static void stubStartSuccess(WireMockServer wireMock, String sessionId) {
        wireMock.stubFor(post(urlEqualTo("/api/rest/tests/start"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "createdSessions": [
                                    {"session": "%s", "completed": false}
                                  ]
                                }
                                """.formatted(sessionId))));
    }

    public static void stubStartSyncSuccess(WireMockServer wireMock, String sessionId) {
        wireMock.stubFor(post(urlEqualTo("/api/rest/tests/start"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "createdSessions": [
                                    {
                                      "session": "%s",
                                      "completed": true
                                    }
                                  ]
                                }
                                """.formatted(sessionId))));
    }

    public static void stubStatusLogs(WireMockServer wireMock, String sessionId, String... logs) {
        String logsJson = logs == null || logs.length == 0
                ? "[]"
                : "[\"" + String.join("\",\"", logs) + "\"]";
        wireMock.stubFor(post(urlEqualTo("/api/rest/tests/status"))
                .withRequestBody(matchingJsonPath("$.withLogs", equalTo("true")))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "sessions": [
                                    {"session": "%s", "completed": true, "logs": %s}
                                  ]
                                }
                                """.formatted(sessionId, logsJson))));
    }

    public static void stubStatusReport(WireMockServer wireMock, String sessionId, String report) {
        wireMock.stubFor(post(urlEqualTo("/api/rest/tests/status"))
                .withRequestBody(matchingJsonPath("$.withReports", equalTo("true")))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "sessions": [
                                    {"session": "%s", "completed": true, "report": "%s"}
                                  ]
                                }
                                """.formatted(sessionId, report.replace("\"", "\\\"")))));
    }

    public static void stubStatusReportAndLogs(WireMockServer wireMock, String sessionId, String report, String... logs) {
        String logsJson = logs == null || logs.length == 0
                ? "[]"
                : "[\"" + String.join("\",\"", logs) + "\"]";
        wireMock.stubFor(post(urlEqualTo("/api/rest/tests/status"))
                .withRequestBody(matchingJsonPath("$.withReports", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.withLogs", equalTo("true")))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "sessions": [
                                    {"session": "%s", "completed": true, "report": "%s", "logs": %s}
                                  ]
                                }
                                """.formatted(sessionId, report.replace("\"", "\\\""), logsJson))));
    }

    public static void stubPdfReportSuccess(WireMockServer wireMock, String sessionId, byte[] pdfContent) {
        wireMock.stubFor(get(urlEqualTo("/api/rest/tests/report/" + sessionId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/pdf")
                        .withBody(pdfContent)));
    }
}
