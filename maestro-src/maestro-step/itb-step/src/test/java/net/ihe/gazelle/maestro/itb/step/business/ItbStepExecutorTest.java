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
package net.ihe.gazelle.maestro.itb.step.business;

import net.ihe.gazelle.itb.gateway.business.ItbReportingService;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbResult;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbSystem;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbTestSession;
import net.ihe.gazelle.itb.gateway.technical.client.ItbHttpClient;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInput;
import net.ihe.gazelle.itb.gateway.technical.dao.ItbSessionStoreImpl;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItbStepExecutorTest {

    private static final String SESSION_ID = "SESSION-123";

    @Test
    void execute_registersFutureInDB_withSessionId_fromHandler() {
        ItbHandler handler = new ItbHandlerMock(SESSION_ID);
        ItbSessionStoreImpl store = new ItbSessionStoreImpl();
        ItbStepExecutor runner = new ItbStepExecutor(handler, store);
        StepRun stepRun = new StepRun(step(), List.of());
        ItbReportingService service =
              new ItbReportingService(new ItbHttpClient("baseUrl", "itbApiKey"), store);

        CompletableFuture<StepRunReport> reportFuture =
              CompletableFuture.supplyAsync(() -> runner.execute(stepRun));

        await()
              .atMost(2, TimeUnit.SECONDS)
              .until(() -> store.get(SESSION_ID) != null);

        service.receiveReporting(getItbReporting());
        StepRunReport report = reportFuture.join();
        assertEquals(StepResult.PASSED, report.getResult());
    }

    @Test
    void execute_syncMode_completesWithoutCallback() {
        ItbHandler handler = new ItbHandlerMock(SESSION_ID);
        ItbSessionStoreImpl store = new ItbSessionStoreImpl();
        ItbStepExecutor runner = new ItbStepExecutor(handler, store);
        StepRun stepRun = new StepRun(syncStep(), List.of());

        StepRunReport report = runner.execute(stepRun);

        assertEquals(StepResult.PASSED, report.getResult());
        assertNotNull(report.getOutputs());
    }

    @Test
    void execute_syncMode_doesNotDuplicateReferencedBinaryInput() {
        ItbHandlerMock handler = new ItbHandlerMock(SESSION_ID);
        ItbSessionStoreImpl store = new ItbSessionStoreImpl();
        ItbStepExecutor runner = new ItbStepExecutor(handler, store);
        StepRun stepRun = new StepRun(syncStepWithReferencedBinaryInput(),
              List.of(new ByteArrayProperty("inputFile", "bytes".getBytes(StandardCharsets.UTF_8))));

        StepRunReport report = runner.execute(stepRun);

        assertEquals(StepResult.PASSED, report.getResult());
        assertNotNull(handler.getLastStartAndWaitRequest());
        assertNotNull(handler.getLastStartAndWaitRequest().getInputMapping());
        List<String> inputNames = handler.getLastStartAndWaitRequest().getInputMapping()
              .stream().map(entry -> entry.getInput().getName()).toList();
        assertEquals(2, inputNames.size());
        assertTrue(inputNames.contains("lab_report_fhir"));
        assertTrue(inputNames.contains("uploadedFileName"));
    }

    @Test
    void execute_syncMode_userInputMapMode_buildsSingleMappedInput() {
        ItbHandlerMock handler = new ItbHandlerMock(SESSION_ID);
        ItbSessionStoreImpl store = new ItbSessionStoreImpl();
        ItbStepExecutor runner = new ItbStepExecutor(handler, store);
        StepRun stepRun = new StepRun(syncStepWithReferencedBinaryInputAndMapMode(),
              List.of(new ByteArrayProperty("inputFile", "bytes".getBytes(StandardCharsets.UTF_8))));

        StepRunReport report = runner.execute(stepRun);

        assertEquals(StepResult.PASSED, report.getResult());
        assertNotNull(handler.getLastStartAndWaitRequest());
        assertNotNull(handler.getLastStartAndWaitRequest().getInputMapping());
        assertEquals(1, handler.getLastStartAndWaitRequest().getInputMapping().size());

        var mappedInput = handler.getLastStartAndWaitRequest().getInputMapping().getFirst().getInput();
        assertEquals("userInput", mappedInput.getName());
        assertEquals("map", mappedInput.getType());
        assertNotNull(mappedInput.getItem());
        assertEquals(2, mappedInput.getItem().size());
        List<String> itemNames = mappedInput.getItem().stream().map(ItbInput::getName).toList();
        assertTrue(itemNames.contains("lab_report_fhir"));
        assertTrue(itemNames.contains("uploadedFileName"));
    }


    private ItbReporting getItbReporting() {
        return new ItbReporting(
                new ItbSystem(64L,"shortName", "fullName"),
                new ItbTestSession("testSuite", "testCase", SESSION_ID),
                ITB_REPORT
        ).setResult(ItbResult.SUCCESS);
    }

    private static final String ITB_REPORT = """
            <ItbReport><n1/><n2/><n3/><n4/><n5/><n6/><n7/><status>SUCCESS</status></ItbReport>
            """;

    private Step step() {
        return new Step()
                .setName("itb")
                .setType(ItbStepDefinition.TYPE)
                .setProperties(List.of(
                        new StringProperty(ItbStepDefinition.ACTOR_ID, "actor"),
                        new StringProperty(ItbStepDefinition.TEST_CASE, "test1"),
                        new StringProperty("systemID", "system"),
                        new StringProperty("note", "hello world"),
                        new ByteArrayProperty("payload", "bytes".getBytes(StandardCharsets.UTF_8))
                ));
    }

    private Step syncStep() {
        return new Step()
                .setName("itb-sync")
                .setType(ItbStepDefinition.TYPE)
                .setProperties(List.of(
                        new StringProperty(ItbStepDefinition.ACTOR_ID, "actor"),
                        new StringProperty(ItbStepDefinition.TEST_CASE, "test1"),
                        new StringProperty("systemID", "system"),
                        new StringProperty(ItbStepDefinition.EXECUTION_MODE, "SYNC")
                ));
    }

    private Step syncStepWithReferencedBinaryInput() {
        return new Step()
              .setName("itb-sync")
              .setType(ItbStepDefinition.TYPE)
              .setProperties(List.of(
                    new StringProperty(ItbStepDefinition.ACTOR_ID, "actor"),
                    new StringProperty(ItbStepDefinition.TEST_CASE, "test1"),
                    new StringProperty("systemID", "system"),
                    new StringProperty(ItbStepDefinition.EXECUTION_MODE, "SYNC"),
                    new ByteArrayProperty("lab_report_fhir", "${inputFile}"),
                    new StringProperty("uploadedFileName", "1-1234-W7.json")
              ));
    }

    private Step syncStepWithReferencedBinaryInputAndMapMode() {
        return new Step()
              .setName("itb-sync")
              .setType(ItbStepDefinition.TYPE)
              .setProperties(List.of(
                    new StringProperty(ItbStepDefinition.ACTOR_ID, "actor"),
                    new StringProperty(ItbStepDefinition.TEST_CASE, "test1"),
                    new StringProperty("systemID", "system"),
                    new StringProperty(ItbStepDefinition.EXECUTION_MODE, "SYNC"),
                    new StringProperty(ItbStepDefinition.ITB_PAYLOAD_MODE, "USER_INPUT_MAP"),
                    new ByteArrayProperty("lab_report_fhir", "${inputFile}"),
                    new StringProperty("uploadedFileName", "Valid - 1-1234-W7.json")
              ));
    }
}
