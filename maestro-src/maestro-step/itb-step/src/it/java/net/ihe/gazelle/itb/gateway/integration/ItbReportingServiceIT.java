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

import net.ihe.gazelle.itb.gateway.business.ItbReportingService;
import net.ihe.gazelle.itb.gateway.business.ItbSessionStore;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbResult;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbSystem;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbTestSession;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItbReportingServiceIT extends ItbWireMockITSupport {

    @Test
    void receiveReporting_shouldEnrichReportCompleteFutureAndCleanupSession() {
        String sessionId = "REPORTING-SESSION";
        byte[] expectedPdf = "binary-pdf".getBytes(StandardCharsets.UTF_8);
        String expectedLogs = "LOG-CONTENT";

        stubStatusLogs(sessionId, expectedLogs);
        stubPdfReportSuccess(sessionId, expectedPdf);

        ItbSessionStore sessionStore = inMemorySessionStore();
        CompletableFuture<ItbReporting> reportFuture = new CompletableFuture<>();
        sessionStore.add(sessionId, reportFuture);

        ItbReportingService service = new ItbReportingService(itbHttpClient, sessionStore);
        ItbReporting callbackReport = new ItbReporting(
                new ItbSystem(64L, "SUT", "System Under Test"),
                new ItbTestSession("suite-001", "case-001", sessionId),
                "<ItbReport><n1/><n2/><n3/><n4/><n5/><n6/><n7/><status>SUCCESS</status></ItbReport>"
        );

        service.receiveReporting(callbackReport);

        assertTrue(reportFuture.isDone());
        ItbReporting completedReport = reportFuture.join();
        assertSame(callbackReport, completedReport);
        assertEquals(ItbResult.SUCCESS, completedReport.getResult());
        assertArrayEquals(expectedPdf, completedReport.getPdfReport());
        assertEquals(expectedLogs, completedReport.getLogs());
        assertNull(sessionStore.get(sessionId));

        wireMock.verify(postRequestedFor(urlEqualTo("/api/rest/tests/status")));
        wireMock.verify(getRequestedFor(urlEqualTo("/api/rest/tests/report/" + sessionId)));
    }
}
