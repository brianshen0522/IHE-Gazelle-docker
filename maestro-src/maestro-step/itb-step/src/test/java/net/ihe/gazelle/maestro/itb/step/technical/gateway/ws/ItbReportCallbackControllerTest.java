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
package net.ihe.gazelle.maestro.itb.step.technical.gateway.ws;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.itb.gateway.business.ItbReportingService;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbTestSession;
import net.ihe.gazelle.itb.gateway.technical.client.ItbHttpClient;
import net.ihe.gazelle.itb.gateway.technical.dao.ItbSessionStoreImpl;
import net.ihe.gazelle.itb.gateway.technical.ws.ItbReportCallbackController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItbReportCallbackControllerTest {

    static ItbSessionStoreImpl store;
    static ItbReportingService itbReportingService;

    @BeforeAll
    static void init() {
        store = new ItbSessionStoreImpl();
        itbReportingService = new ItbReportingService(new ItbHttpClient("baseUrl", "apiKey"), store);
    }

    @Test
    void triggerReportGeneration_returns404_whenSessionNotFound() {
        ItbReportCallbackController controller = new ItbReportCallbackController(itbReportingService);
        try (Response response = controller.triggerReportGeneration(getItpReport("missing", VALID_REPORT))) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void triggerReportGeneration_completesFuture_and_returns200_onSuccess() {
        String sessionId = "SESSION-1";
        CompletableFuture<ItbReporting> future = new CompletableFuture<>();
        store.add(sessionId, future);

        ItbReportCallbackController controller = new ItbReportCallbackController(itbReportingService);
        try (Response response = controller.triggerReportGeneration(getItpReport(sessionId, VALID_REPORT))) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertTrue(future.isDone());
            assertNull(store.get(sessionId));
        }
    }

    @Test
    void triggerReportGeneration_returns400_whenServiceThrowsCatchException() {
        String sessionId = "SESSION-2";
        store.add(sessionId, new CompletableFuture<>());

        ItbReportCallbackController controller = new ItbReportCallbackController(itbReportingService);
        try (Response response = controller.triggerReportGeneration(getItpReport(sessionId, INVALID_REPORT))) {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    private static final String VALID_REPORT = "<ItbReport><n1/><n2/><n3/><n4/><n5/><n6/><n7/><status>SUCCESS</status></ItbReport>";
    private static final String INVALID_REPORT = "<ItbReport><n1/><n2/><n3/><n4/><n5/><n6/><n7/><status></status></ItbReport>";

    private static ItbReporting getItpReport(String sessionId, String report) {
        ItbTestSession session = new ItbTestSession();
        session.setTestSessionIdentifier(sessionId);
        session.setTestCaseIdentifier("TC");
        ItbReporting dto = new ItbReporting();
        dto.setTestSession(session);
        dto.setTestReport(report);
        return dto;
    }
}
