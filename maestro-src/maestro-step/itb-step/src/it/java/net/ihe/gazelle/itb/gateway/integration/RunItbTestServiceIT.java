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

import net.ihe.gazelle.itb.gateway.business.ItbExecutionMode;
import net.ihe.gazelle.itb.gateway.business.ItbSessionStore;
import net.ihe.gazelle.itb.gateway.business.RunItbTestService;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbResult;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStartRequest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunItbTestServiceIT extends ItbWireMockITSupport {

    @Test
    void runTests_shouldStartItbAndRegisterFutureInSessionStore() {
        String itbSessionId = "RUN-SERVICE-SESSION";
        stubStartSuccess(itbSessionId);

        ItbSessionStore sessionStore = inMemorySessionStore();
        RunItbTestService service = new RunItbTestService(itbHttpClient, sessionStore);

        CompletableFuture<ItbReporting> reportFuture = service.runTests(buildRequest());

        assertNotNull(reportFuture);
        assertFalse(reportFuture.isDone());
        assertSame(reportFuture, sessionStore.get(itbSessionId));
    }

    @Test
    void runTests_syncMode_shouldReturnCompletedFutureWithoutSessionRegistration() {
        String report = "<ItbReport><n1/><n2/><n3/><n4/><n5/><n6/><n7/><status>SUCCESS</status></ItbReport>";
        byte[] expectedPdf = "sync-pdf".getBytes(StandardCharsets.UTF_8);
        stubStartSyncSuccess("SYNC-RUN-SESSION");
        stubStatusReportAndLogs("SYNC-RUN-SESSION", report, "sync-log-line");
        stubPdfReportSuccess("SYNC-RUN-SESSION", expectedPdf);

        ItbSessionStore sessionStore = inMemorySessionStore();
        RunItbTestService service = new RunItbTestService(itbHttpClient, sessionStore);

        CompletableFuture<ItbReporting> reportFuture = service.runTests(buildRequest(), ItbExecutionMode.SYNC, 20_000L);

        assertTrue(reportFuture.isDone());
        ItbReporting itbReporting = reportFuture.join();
        assertEquals(ItbResult.SUCCESS, itbReporting.getResult());
        assertArrayEquals(expectedPdf, itbReporting.getPdfReport());
        assertEquals("sync-log-line", itbReporting.getLogs());
        assertNull(sessionStore.get("SYNC-RUN-SESSION"));
    }

    private ItbStartRequest buildRequest() {
        return new ItbStartRequest()
                .setSystem("sys-001")
                .setActor("actor-001")
                .setTestSuite(List.of("suite-001"));
    }
}
