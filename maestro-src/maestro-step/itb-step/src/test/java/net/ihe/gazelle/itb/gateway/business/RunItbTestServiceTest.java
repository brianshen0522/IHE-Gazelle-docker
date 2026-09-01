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
package net.ihe.gazelle.itb.gateway.business;

import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbResult;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbSystem;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbTestSession;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInput;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInputMappingEntry;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStartRequest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RunItbTestServiceTest {

    private static final String SESSION_ID = "SESSION-TEST";
    private static final String VALID_XML_REPORT =
            "<ItbReport><n1/><n2/><n3/><n4/><n5/><n6/><n7/><status>SUCCESS</status></ItbReport>";

    @Test
    void runTestsAsyncModeRegistersPendingFutureAndSendsValidatedCopy() {
        RecordingItbClient itbClient = new RecordingItbClient();
        InMemorySessionStore sessionStore = new InMemorySessionStore();
        RunItbTestService service = new RunItbTestService(itbClient, sessionStore);

        ItbInput textInput = new ItbInput().setName("description").setType("text").setValue("hello");
        ItbInput binaryInput = new ItbInput().setName("document").setType("file").setValue(base64("payload"));
        ItbInput nestedMap = new ItbInput()
                .setName("credentials")
                .setItem(List.of(new ItbInput().setName("token").setType("uuid").setValue("abc-123")));
        ItbStartRequest request = validRequest().setInputMapping(List.of(
                new ItbInputMappingEntry().setInput(textInput),
                new ItbInputMappingEntry().setInput(binaryInput),
                new ItbInputMappingEntry().setInput(nestedMap)
        ));

        CompletableFuture<ItbReporting> future = service.runTests(request);

        assertFalse(future.isDone());
        assertSame(future, sessionStore.get(SESSION_ID));
        assertNotSame(request, itbClient.asyncRequest);
        assertNotSame(request.getInputMapping().getFirst(), itbClient.asyncRequest.getInputMapping().get(0));
        assertNotSame(textInput, itbClient.asyncRequest.getInputMapping().get(0).getInput());

        assertEquals("string", itbClient.asyncRequest.getInputMapping().get(0).getInput().getType());
        assertEquals("binary", itbClient.asyncRequest.getInputMapping().get(1).getInput().getType());
        assertEquals("BASE64", itbClient.asyncRequest.getInputMapping().get(1).getInput().getEmbeddingMethod());
        assertEquals("map", itbClient.asyncRequest.getInputMapping().get(2).getInput().getType());
        assertEquals("string", itbClient.asyncRequest.getInputMapping().get(2).getInput().getItem().get(0).getType());

        assertEquals("text", textInput.getType());
        assertEquals("file", binaryInput.getType());
        assertNull(binaryInput.getEmbeddingMethod());
        assertNull(nestedMap.getType());
    }

    @Test
    void runTestsAsyncModeInfersTypesFromInputValues() {
        RecordingItbClient itbClient = new RecordingItbClient();
        RunItbTestService service = new RunItbTestService(itbClient, new InMemorySessionStore());

        ItbStartRequest request = validRequest().setInputMapping(List.of(
                new ItbInputMappingEntry().setInput(new ItbInput().setName("amount").setValue("42.5")),
                new ItbInputMappingEntry().setInput(new ItbInput().setName("attachment").setValue(base64("bin"))),
                new ItbInputMappingEntry().setInput(new ItbInput().setName("comment").setValue("plain text"))
        ));

        service.runTests(request);

        assertEquals("number", itbClient.asyncRequest.getInputMapping().get(0).getInput().getType());
        assertEquals("binary", itbClient.asyncRequest.getInputMapping().get(1).getInput().getType());
        assertEquals("BASE64", itbClient.asyncRequest.getInputMapping().get(1).getInput().getEmbeddingMethod());
        assertEquals("string", itbClient.asyncRequest.getInputMapping().get(2).getInput().getType());
    }

    @Test
    void runTestsSyncModeReturnsEnrichedReporting() {
        RecordingItbClient itbClient = new RecordingItbClient();
        itbClient.syncReporting = reportingWithSession(SESSION_ID, "test-case");
        itbClient.logs = "session logs";
        itbClient.pdfReport = "pdf".getBytes(StandardCharsets.UTF_8);
        RunItbTestService service = new RunItbTestService(itbClient, new InMemorySessionStore());

        CompletableFuture<ItbReporting> future = service.runTests(validRequest(), ItbExecutionMode.SYNC, 5000L);
        ItbReporting reporting = future.join();

        assertSame(itbClient.syncReporting, reporting);
        assertEquals(5000L, itbClient.syncTimeoutMs);
        assertEquals(ItbResult.SUCCESS, reporting.getResult());
        assertEquals("session logs", reporting.getLogs());
        assertArrayEquals("pdf".getBytes(StandardCharsets.UTF_8), reporting.getPdfReport());
        assertEquals(SESSION_ID, itbClient.logsSessionId);
        assertEquals(SESSION_ID, itbClient.pdfSessionId);
        assertEquals("test-case", itbClient.pdfTestCaseIdentifier);
    }

    @Test
    void runTestsSyncModeRequiresStrictlyPositiveTimeout() {
        RunItbTestService service = new RunItbTestService(new RecordingItbClient(), new InMemorySessionStore());
        ItbStartRequest zeroTimeoutRequest = validRequest();
        ItbStartRequest negativeTimeoutRequest = validRequest();

        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(zeroTimeoutRequest, ItbExecutionMode.SYNC, 0L));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(negativeTimeoutRequest, ItbExecutionMode.SYNC, -1L));
    }

    @Test
    void runTestsRejectsMissingCoreRequestData() {
        RunItbTestService service = new RunItbTestService(new RecordingItbClient(), new InMemorySessionStore());
        ItbStartRequest blankSystemRequest = validRequest().setSystem(" ");
        ItbStartRequest missingActorRequest = validRequest().setActor(null);
        ItbStartRequest missingTestsRequest = validRequest().setTestCase(null).setTestSuite(List.of());

        assertThrows(RunItbTestServiceException.class, () -> service.runTests(null));
        assertThrows(RunItbTestServiceException.class, () -> service.runTests(blankSystemRequest));
        assertThrows(RunItbTestServiceException.class, () -> service.runTests(missingActorRequest));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(missingTestsRequest));
    }

    @Test
    void runTestsRejectsInvalidIdentifiersAndMappingEntries() {
        RunItbTestService service = new RunItbTestService(new RecordingItbClient(), new InMemorySessionStore());
        List<ItbInputMappingEntry> inputMappingWithNullEntry = new ArrayList<>();
        inputMappingWithNullEntry.add(null);
        ItbStartRequest blankTestCaseIdentifierRequest = validRequest().setTestCase(List.of(" "));
        ItbStartRequest blankMappingIdentifierRequest = validRequest().setInputMapping(List.of(
                new ItbInputMappingEntry()
                        .setTestCase(List.of(""))
                        .setInput(new ItbInput().setName("name").setType("string").setValue("value"))
        ));
        ItbStartRequest nullMappingEntryRequest = validRequest().setInputMapping(inputMappingWithNullEntry);
        ItbStartRequest nullInputRequest = validRequest().setInputMapping(List.of(
                new ItbInputMappingEntry().setInput(null)
        ));

        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(blankTestCaseIdentifierRequest));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(blankMappingIdentifierRequest));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(nullMappingEntryRequest));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(nullInputRequest));
    }

    @Test
    void runTestsRejectsInvalidScalarInputs() {
        RunItbTestService service = new RunItbTestService(new RecordingItbClient(), new InMemorySessionStore());
        ItbStartRequest blankInputNameRequest =
                requestWithInput(new ItbInput().setName("").setType("string").setValue("v"));
        ItbStartRequest emptyStringValueRequest =
                requestWithInput(new ItbInput().setName("text").setType("string").setValue(""));
        ItbStartRequest invalidNumberRequest =
                requestWithInput(new ItbInput().setName("count").setType("number").setValue("NaN?"));
        ItbStartRequest invalidBinaryRequest =
                requestWithInput(new ItbInput().setName("file").setType("binary").setValue("not-base64"));
        ItbStartRequest unsupportedTypeRequest =
                requestWithInput(new ItbInput().setName("x").setType("unsupported").setValue("v"));

        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(blankInputNameRequest));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(emptyStringValueRequest));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(invalidNumberRequest));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(invalidBinaryRequest));
        assertThrows(RunItbTestServiceException.class,
                () -> service.runTests(unsupportedTypeRequest));
    }

    @Test
    void runTestsRejectsNonMapInputsContainingNestedItems() {
        RunItbTestService service = new RunItbTestService(new RecordingItbClient(), new InMemorySessionStore());
        ItbInput invalidInput = new ItbInput()
                .setName("payload")
                .setType("string")
                .setItem(List.of(new ItbInput().setName("child").setType("string").setValue("value")));
        ItbStartRequest invalidRequest = requestWithInput(invalidInput);

        assertThrows(RunItbTestServiceException.class, () -> service.runTests(invalidRequest));
    }

    private ItbStartRequest validRequest() {
        return new ItbStartRequest()
                .setSystem("system-1")
                .setActor("actor-1")
                .setTestCase(List.of("test-case"));
    }

    private ItbStartRequest requestWithInput(ItbInput input) {
        return validRequest().setInputMapping(List.of(new ItbInputMappingEntry().setInput(input)));
    }

    private ItbReporting reportingWithSession(String sessionId, String testCaseIdentifier) {
        return new ItbReporting(
                new ItbSystem(1L, "short", "full"),
                new ItbTestSession("suite", testCaseIdentifier, sessionId),
                VALID_XML_REPORT
        );
    }

    private String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static final class RecordingItbClient implements ItbClient {

        private ItbStartRequest asyncRequest;
        private long syncTimeoutMs;
        private ItbReporting syncReporting = new ItbReporting();
        private byte[] pdfReport = new byte[0];
        private String logs;
        private String logsSessionId;
        private String pdfSessionId;
        private String pdfTestCaseIdentifier;

        @Override
        public String startTest(ItbStartRequest startRequest) {
            this.asyncRequest = startRequest;
            return SESSION_ID;
        }

        @Override
        public ItbReporting startTestAndWait(ItbStartRequest startRequest, long timeoutMs) {
            this.syncTimeoutMs = timeoutMs;
            return syncReporting;
        }

        @Override
        public String getTestLogs(String sessionID) {
            this.logsSessionId = sessionID;
            return logs;
        }

        @Override
        public byte[] requestPDFReport(String sessionID, String testCaseName) {
            this.pdfSessionId = sessionID;
            this.pdfTestCaseIdentifier = testCaseName;
            return pdfReport;
        }
    }

    private static final class InMemorySessionStore implements ItbSessionStore {

        private final Map<String, CompletableFuture<ItbReporting>> futuresBySessionId = new LinkedHashMap<>();

        @Override
        public CompletableFuture<ItbReporting> get(String sessionId) {
            return futuresBySessionId.get(sessionId);
        }

        @Override
        public void add(String sessionId, CompletableFuture<ItbReporting> future) {
            futuresBySessionId.put(sessionId, future);
        }

        @Override
        public void remove(String sessionId) {
            futuresBySessionId.remove(sessionId);
        }
    }
}
