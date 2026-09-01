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

package net.ihe.gazelle.maestrorecording.adapter;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import com.kereval.gazelle.datahouse.api.business.record.Reference;
import com.kereval.gazelle.datahouse.api.business.record.RefType;
import net.ihe.gazelle.maestro.api.business.property.*;
import net.ihe.gazelle.maestro.api.business.testreport.Result;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ReferencePlan;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DatahouseTestReportRecordServiceTest {

    private DatahouseTestReportRecordingService service;
    private RecordItemClientMock recordItemClientMock;

    @BeforeEach
    void setUp() {
        recordItemClientMock = new RecordItemClientMock();
        service = new DatahouseTestReportRecordingService(recordItemClientMock, "http://dh", emptyDissectorProvider());
    }

    @Test
    void records_inputs_as_attachments_and_updates_references() {
        ByteArrayProperty firstInput = binaryInput("in1", "alpha").setFileName("alpha.xml");
        ByteArrayProperty secondInput = binaryInput("in2", "beta");

        TestRunReport run = newTestRun("run-inputs")
                .addInput(firstInput)
                .addInput(secondInput);
        TestReport report = newReport("R-1", run);

        String reportLocation = service.recordTestReport(report);

        assertThat(reportLocation).isEqualTo("http://dh/items/itm-1");
        assertThat(recordItemClientMock.recordedItems).hasSize(1);
        assertThat(recordItemClientMock.uploadedAttachmentsBatches).hasSize(2);
        assertThat(recordItemClientMock.uploadedAttachmentsBatches.getFirst().getFirst().getFilename()).isEqualTo("alpha.xml");
        assertThat(recordItemClientMock.uploadedAttachmentsBatches.getFirst().getFirst().getType())
              .isEqualTo(DatahouseTestReportRecordingService.DEFAULT_ATTACHMENT_MIME_TYPE);
        assertThat(recordItemClientMock.uploadedAttachmentsBatches.get(1).getFirst().getFilename()).isEqualTo("in2");
        assertThat(recordItemClientMock.uploadedAttachmentsBatches.get(1).getFirst().getType())
              .isEqualTo(DatahouseTestReportRecordingService.DEFAULT_ATTACHMENT_MIME_TYPE);

        List<Property> inputs = firstRun(report).getInputs();
        assertThat(inputs).hasSize(2);
        assertBinaryAttachment(inputs.get(0), "/attachments/att-1");
        assertBinaryAttachment(inputs.get(1), "/attachments/att-2");

        Item testReportItem = recordedItemOfType(DatahouseTestReportRecordingService.TEST_REPORT_TYPE);
        assertThat(testReportItem.getReferences()).filteredOn(reference -> "in1".equals(reference.getName()))
              .singleElement()
              .satisfies(reference -> {
                 assertEquals(RefType.ATTACHMENT, reference.getRefType());
                 assertEquals(DatahouseTestReportRecordingService.INPUT_ATTACHMENT_REFERENCE_TYPE, reference.getType());
              });
        assertThat(testReportItem.getReferences()).filteredOn(reference -> "in2".equals(reference.getName()))
              .singleElement()
              .satisfies(reference -> {
                 assertEquals(RefType.ATTACHMENT, reference.getRefType());
                 assertEquals(DatahouseTestReportRecordingService.INPUT_ATTACHMENT_REFERENCE_TYPE, reference.getType());
              });
    }

    @Test
    void keeps_explicit_mime_type_on_uploaded_attachments() {
        ByteArrayProperty typedInput = binaryInput("in1", "alpha").setMimeType("application/xml");
        TestRunReport run = newTestRun("run-mime").addInput(typedInput);
        TestReport report = newReport("R-mime", run);

        service.recordTestReport(report);

        assertThat(recordItemClientMock.uploadedAttachmentsBatches).hasSize(1);
        assertThat(recordItemClientMock.uploadedAttachmentsBatches.getFirst().getFirst().getType())
              .isEqualTo("application/xml");
    }

    @Test
    void keeps_non_binary_inputs_inline() {
        StringProperty textInput = new StringProperty("text", "hello");
        BooleanProperty booleanInput = new BooleanProperty("flag", true);
        IntegerProperty numberInput = new IntegerProperty("count", 42);

        TestRunReport run = newTestRun("run-inline")
                .addInput(textInput)
                .addInput(booleanInput)
                .addInput(numberInput);
        TestReport report = newReport("R-inline", run);

        service.recordTestReport(report);

        assertThat(recordItemClientMock.uploadedAttachmentsBatches).isEmpty();

        List<Property> inputs = firstRun(report).getInputs();
        assertThat(inputs).hasSize(3);
        assertThat(inputs.get(0)).isInstanceOf(StringProperty.class);
        assertEquals("hello", inputs.get(0).getValue());
        assertThat(inputs.get(1)).isInstanceOf(BooleanProperty.class);
        assertTrue((Boolean) inputs.get(1).getValue());
        assertThat(inputs.get(2)).isInstanceOf(IntegerProperty.class);
        assertEquals(42, (Integer) inputs.get(2).getValue());
    }

    @Test
    void records_step_outputs_using_dissection_plan() {
        service = new DatahouseTestReportRecordingService(recordItemClientMock, "http://dh", staticDissectorProvider());

        StepRunReport step = new StepRunReport()
                .setStepName("step-outputs")
                .addOutput(new ByteArrayProperty(StaticStepOutputDissector.ROOT_OUTPUT_NAME, "{}".getBytes(StandardCharsets.UTF_8)))
                .addOutput(new ByteArrayProperty(StaticStepOutputDissector.SIBLING_OUTPUT_NAME, "{}".getBytes(StandardCharsets.UTF_8)))
                .addOutput(new ByteArrayProperty(StaticStepOutputDissector.ATTACHMENT_OUTPUT_NAME, "pdf".getBytes(StandardCharsets.UTF_8)));

        TestRunReport run = newTestRun("run-outputs").addStepRunReport(step);
        TestReport report = newReport("R-outputs", run);

        String reportLocation = service.recordTestReport(report);

        assertThat(recordItemClientMock.recordedItems).hasSize(5);
        assertThat(recordItemClientMock.uploadedAttachmentsBatches).hasSize(2);
        assertThat(reportLocation).isEqualTo("http://dh/items/itm-5");

        Map<String, String> itemIdsByType = itemIdsByType();

        List<Property> outputs = firstStep(report).getOutputs();
        assertThat(outputs).hasSize(3);
        assertBinaryAttachment(outputs.get(0), "http://dh/items/" + itemIdsByType.get(StaticStepOutputDissector.ROOT_ITEM_TYPE));
        assertBinaryAttachment(outputs.get(1), "http://dh/items/" + itemIdsByType.get(StaticStepOutputDissector.SIBLING_ITEM_TYPE));
        assertBinaryAttachment(outputs.get(2), "/attachments/att-1");

        Item rootItem = recordedItemOfType(StaticStepOutputDissector.ROOT_ITEM_TYPE);
        assertThat(referenceNames(rootItem))
                .containsExactlyInAnyOrder(
                        StaticStepOutputDissector.CHILD_REFERENCE_NAME,
                        StaticStepOutputDissector.SIBLING_REFERENCE_NAME,
                        StaticStepOutputDissector.ATTACHMENT_REFERENCE_NAME,
                        StaticStepOutputDissector.DETACHED_REFERENCE_NAME
                );

        Item childItem = recordedItemOfType(StaticStepOutputDissector.CHILD_ITEM_TYPE);
        assertThat(referenceNames(childItem))
                .containsExactly(StaticStepOutputDissector.GRANDCHILD_REFERENCE_NAME);

        Item testReportItem = recordedItemOfType(DatahouseTestReportRecordingService.TEST_REPORT_TYPE);
        assertThat(referenceNames(testReportItem))
                .contains(StaticStepOutputDissector.ATTACHMENT_REFERENCE_NAME,
                        StaticStepOutputDissector.DETACHED_REFERENCE_NAME,
                        StaticStepOutputDissector.REPORT_REFERENCE_NAME,
                        StaticStepOutputDissector.SIBLING_REFERENCE_NAME);
    }

    @Test
    void records_sub_reports_as_item_references() {
        TestReport subReport = newReport("R-child", newTestRun("run-child"))
              .setTestSuiteName("child-suite")
              .setResult(Result.PASSED);
        TestReport parentReport = newReport("R-parent", newTestRun("run-parent"))
              .setTestSuiteName("parent-suite")
              .addSubReport(subReport);

        service.recordTestReport(parentReport);

        assertThat(recordItemClientMock.recordedItems).hasSize(2);
        assertThat(recordItemClientMock.recordedItems.stream()
              .map(Item.class::cast)
              .flatMap(item -> item.getReferences().stream())
              .map(Reference::getName))
              .contains("child-suite");
    }

    @Test
    void callback_added_child_references_are_persisted() {
        service = new DatahouseTestReportRecordingService(recordItemClientMock, "http://dh", callbackDissectorProvider());
        CallbackStepOutputDissector.callbackFired.set(false);

        StepRunReport step = new StepRunReport()
              .setStepName("step-outputs")
              .addOutput(new ByteArrayProperty("validation-report", "{}".getBytes(StandardCharsets.UTF_8)));

        TestRunReport run = newTestRun("run-callback")
              .addInput(binaryInput("in1", "alpha"))
              .addStepRunReport(step);
        TestReport report = newReport("R-callback", run);

        service.recordTestReport(report);
        assertTrue(CallbackStepOutputDissector.callbackFired.get(), "Expected callback to be invoked");

        Item callbackItem = recordedItemOfType("CALLBACK_ITEM");
        assertThat(referenceNames(callbackItem)).contains("in1");
    }

    @Test
    void callback_is_triggered_for_pre_resolved_input_references_without_reupload() {
        service = new DatahouseTestReportRecordingService(recordItemClientMock, "http://dh", callbackDissectorProvider());
        CallbackStepOutputDissector.callbackFired.set(false);

        ByteArrayItemProperty preResolvedInput = new ByteArrayItemProperty("in1", null)
              .setReference("/attachments/att-existing");

        StepRunReport step = new StepRunReport()
              .setStepName("step-pre-resolved-input")
              .addOutput(new ByteArrayProperty("validation-report", "{}".getBytes(StandardCharsets.UTF_8)));

        TestRunReport run = newTestRun("run-pre-resolved")
              .addInput(preResolvedInput)
              .addStepRunReport(step);
        TestReport report = newReport("R-pre-resolved", run);

        service.recordTestReport(report);

        assertTrue(CallbackStepOutputDissector.callbackFired.get(), "Expected callback to be invoked");
        assertThat(recordItemClientMock.uploadedAttachmentsBatches).isEmpty();

        Item callbackItem = recordedItemOfType("CALLBACK_ITEM");
        Item testReportItem = recordedItemOfType(DatahouseTestReportRecordingService.TEST_REPORT_TYPE);
        assertThat(referenceNames(callbackItem)).contains("in1");
        assertThat(referenceNames(testReportItem)).contains("in1");
    }

    @Test
    void callback_registry_is_scoped_per_test_run_when_reference_names_overlap() {
        service = new DatahouseTestReportRecordingService(recordItemClientMock, "http://dh", callbackDissectorProvider());
        CallbackStepOutputDissector.callbackFired.set(false);

        StepRunReport firstStep = new StepRunReport()
              .setStepName("step-run-1")
              .addOutput(new ByteArrayProperty("validation-report", "{}".getBytes(StandardCharsets.UTF_8)));
        StepRunReport secondStep = new StepRunReport()
              .setStepName("step-run-2")
              .addOutput(new ByteArrayProperty("validation-report", "{}".getBytes(StandardCharsets.UTF_8)));

        TestRunReport firstRun = newTestRun("run-1")
              .addInput(binaryInput("in1", "alpha"))
              .addStepRunReport(firstStep);
        TestRunReport secondRun = newTestRun("run-2")
              .addInput(binaryInput("in1", "beta"))
              .addStepRunReport(secondStep);

        TestReport report = new TestReport()
              .setUuid("R-overlap")
              .setDateTime(Instant.now())
              .setResult(Result.UNDEFINED)
              .addTestRunReport(firstRun)
              .addTestRunReport(secondRun);

        service.recordTestReport(report);

        List<Item> callbackItems = recordedItemsOfType("CALLBACK_ITEM");
        assertThat(callbackItems).hasSize(2);
        for (Item callbackItem : callbackItems) {
            assertThat(callbackItem.getReferences()).hasSize(1);
            assertEquals("in1", callbackItem.getReferences().getFirst().getName());
            assertEquals(RefType.ATTACHMENT, callbackItem.getReferences().getFirst().getRefType());
        }
        assertThat(callbackItems.stream()
              .map(item -> item.getReferences().getFirst().getValue())
              .collect(Collectors.toSet()))
              .isEqualTo(Set.of("att-1", "att-2"));
    }

    @Test
    void pre_resolved_input_with_invalid_attachment_path_is_ignored() {
        service = new DatahouseTestReportRecordingService(recordItemClientMock, "http://dh", callbackDissectorProvider());
        CallbackStepOutputDissector.callbackFired.set(false);

        ByteArrayItemProperty invalidReferenceInput = new ByteArrayItemProperty("in1", null)
              .setReference("att-existing");

        StepRunReport step = new StepRunReport()
              .setStepName("step-invalid-reference")
              .addOutput(new ByteArrayProperty("validation-report", "{}".getBytes(StandardCharsets.UTF_8)));

        TestRunReport run = newTestRun("run-invalid-reference")
              .addInput(invalidReferenceInput)
              .addStepRunReport(step);
        TestReport report = newReport("R-invalid-reference", run);

        service.recordTestReport(report);

        assertFalse(CallbackStepOutputDissector.callbackFired.get());
        Item testReportItem = recordedItemOfType(DatahouseTestReportRecordingService.TEST_REPORT_TYPE);
        assertThat(referenceNames(testReportItem)).doesNotContain("in1");
    }

    private static ByteArrayProperty binaryInput(String name, String value) {
        return new ByteArrayProperty(name, value.getBytes(StandardCharsets.UTF_8));
    }

    private static TestReport newReport(String uuid, TestRunReport run) {
        return new TestReport()
                .setUuid(uuid)
                .setDateTime(Instant.now())
                .setResult(Result.UNDEFINED)
                .addTestRunReport(run);
    }

    private static TestRunReport newTestRun(String runId) {
        return new TestRunReport()
                .setRunId(runId)
                .setDateTime(Instant.now())
                .setResult(Result.UNDEFINED)
                .setTest(new net.ihe.gazelle.maestro.api.business.testreport.Test().setName("T"));
    }

    private static TestRunReport firstRun(TestReport report) {
        return report.getTestRunReports().getFirst();
    }

    private static StepRunReport firstStep(TestReport report) {
        return firstRun(report).getStepRunReports().getFirst();
    }

    private static void assertBinaryAttachment(Property property, String expectedReference) {
        assertThat(property).isInstanceOf(ByteArrayItemProperty.class);
        ByteArrayItemProperty attachment = (ByteArrayItemProperty) property;
        assertThat(attachment.getReference()).isEqualTo(expectedReference);
        assertNull(attachment.getValue());
    }

    private StepOutputPlanDissectorProvider emptyDissectorProvider() {
        return step -> stepOutputPlan -> { };
    }

    private StepOutputPlanDissectorProvider staticDissectorProvider() {
        return step -> new StaticStepOutputDissector();
    }

    private StepOutputPlanDissectorProvider callbackDissectorProvider() {
        return step -> new CallbackStepOutputDissector();
    }

    private Item recordedItemOfType(String type) {
        return recordItemClientMock.recordedItems.stream()
                .map(Item.class::cast)
                .filter(item -> type.equals(item.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No recorded item of type " + type));
    }

    private List<Item> recordedItemsOfType(String type) {
        return recordItemClientMock.recordedItems.stream()
              .map(Item.class::cast)
              .filter(item -> type.equals(item.getType()))
              .toList();
    }

    private Map<String, String> itemIdsByType() {
        Map<String, String> ids = new HashMap<>();
        for (int i = 0; i < recordItemClientMock.recordedItems.size(); i++) {
            Item item = (Item) recordItemClientMock.recordedItems.get(i);
            ids.put(item.getType(), "itm-" + (i + 1));
        }
        return ids;
    }

    private List<String> referenceNames(Item item) {
        return item.getReferences().stream()
                .map(Reference::getName)
                .toList();
    }

    private static final class CallbackStepOutputDissector implements net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissector {
        private static final AtomicBoolean callbackFired = new AtomicBoolean(false);
        @Override
        public void dissect(StepOutputPlan stepOutputPlan) {
            if (stepOutputPlan.getStepRunReport() == null) {
                return;
            }
            ItemPlan<String> callbackItem = new ItemPlan<>("CALLBACK_ITEM", "{\"k\":\"v\"}", new StringMarshaller());
            stepOutputPlan.onFutureReferenceResolved("in1", (name, attachmentId) ->
                  {
                      callbackFired.set(true);
                      callbackItem.addFutureReferencePlan(
                            ReferencePlan.forAttachmentId("in1", "VALIDATION_INPUT", attachmentId),
                            null
                      );
                  });
            stepOutputPlan.addItemReferencePlan("validation-report", callbackItem);
        }
    }

    private static final class StringMarshaller implements TextMarshaller<String> {
        @Override
        public String marshallAsString(String value) throws MarshallingException {
            return value;
        }

        @Override
        public String unmarshall(String payload) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] marshall(String value) throws MarshallingException {
            return value.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String unmarshall(byte[] payload) {
            throw new UnsupportedOperationException();
        }
    }

}
