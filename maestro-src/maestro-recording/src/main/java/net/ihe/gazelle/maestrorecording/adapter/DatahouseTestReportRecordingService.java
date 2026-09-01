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

import com.kereval.gazelle.datahouse.technical.rest.client.RecordItemClient;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayItemProperty;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import net.ihe.gazelle.maestro.engine.business.TestReportRecordingException;
import net.ihe.gazelle.maestro.engine.business.TestReportRecordingService;
import net.ihe.gazelle.maestro.spi.business.recording.FutureReferenceCallbackRegistry;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlan;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissector;
import net.ihe.gazelle.maestro.spi.business.recording.plan.AttachmentPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ReferencePlan;
import net.ihe.gazelle.maestrorecording.RecordItemService;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.security.business.acl.AccessControlList;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link TestReportRecordingService} that records test reports to the Datahouse system.
 */
public class DatahouseTestReportRecordingService implements TestReportRecordingService {

   static final String TEST_REPORT_TYPE = "TEST_REPORT";
   static final String INPUT_ATTACHMENT_REFERENCE_TYPE = "INPUT_ATTACHMENT";
   static final String DEFAULT_ATTACHMENT_MIME_TYPE = "application/octet-stream";
   private static final String ATTACHMENTS_PATH = "/attachments/";
   private final String datahouseUrl;

   private final StepOutputPlanDissectorProvider dissectorProvider;
   private final RecordItemService recordItemService;
   private final TextSerDes serDes = new JacksonSerDes();


   /**
    * Constructs a new {@code DatahouseTestReportRecordingService}.
    *
    * @param recordItemClient  client used to send records to Datahouse
    * @param datahouseUrl      base URL of the Datahouse service
    * @param dissectorProvider provider used to dissect step outputs for recording
    */
   public DatahouseTestReportRecordingService(RecordItemClient recordItemClient,
                                              String datahouseUrl,
                                              StepOutputPlanDissectorProvider dissectorProvider) {
      this.datahouseUrl = datahouseUrl;
      this.dissectorProvider = dissectorProvider;
      this.recordItemService = new RecordItemService(recordItemClient);
   }

   /**
    * Pre-upload input attachments and update the report to reference them; record step outputs; persist sub-reports;
    * then persist the main TestReport with References[] including inputs and sub-reports.
    *
    * @return the URL of the created item of the main TestReport
    */
   @Override
   public String recordTestReport(TestReport report) {
      String id = recordTestReportWithReferences(report).id();
      return constructUrlToItem(id);
   }

   /**
    * Pre-upload input attachments and update the report to reference them; record step outputs; persist sub-reports;
    * then persist the main TestReport with References[] including inputs and sub-reports.
    *
    * @return the created item ID of the main TestReport
    */
   private ItemReference recordTestReportWithReferences(TestReport report) {
      try {
         ItemPlan<TestReport> testReportPlan = buildTestReportPlan(report, report.getAccessControlList());
         String id = recordItemService.recordPlan(testReportPlan, report.getAccessControlList());
         return new ItemReference(id, constructUrlToItem(id));
      } catch (Exception e) {
         throw new TestReportRecordingException("Failed to record TestReport", e);
      }
   }

   private ItemPlan<TestReport> buildTestReportPlan(TestReport report, AccessControlList acl) {
      List<TestReportReferenceDTO> subReportRefs = new ArrayList<>();
      ItemPlan<TestReport> testReportPlan = new ItemPlan<>(
            TEST_REPORT_TYPE,
            report,
            new TestReportItemMarshaller(subReportRefs, serDes)
      );
      FutureReferenceCallbackRegistry callbackRegistry = new InMemoryFutureReferenceCallbackRegistry();
      Map<TestRunReport, FutureReferenceCallbackRegistry> runScopedRegistries =
            buildRunScopedRegistries(report.getTestRunReports(), callbackRegistry);

      planInputReferences(report.getTestRunReports(), testReportPlan, runScopedRegistries);
      planStepOutputReferences(report.getTestRunReports(), testReportPlan, runScopedRegistries);
      planSubReportReferences(report, subReportRefs, testReportPlan, acl);

      return testReportPlan;
   }

   private void planInputReferences(List<TestRunReport> testRunReports,
                                    ItemPlan<TestReport> testReportPlan,
                                    Map<TestRunReport, FutureReferenceCallbackRegistry> runScopedRegistries) {
      if (testRunReports == null) {
         return;
      }
      for (TestRunReport testRunReport : testRunReports) {
         FutureReferenceCallbackRegistry callbackRegistry = runScopedRegistries.get(testRunReport);
         List<Property> inputs = testRunReport.getInputs();
         if (callbackRegistry != null && inputs != null) {
            planInputPropertiesForRun(testRunReport, inputs, testReportPlan, callbackRegistry);
         }
      }
   }

   private void planInputPropertiesForRun(TestRunReport testRunReport,
                                          List<Property> inputs,
                                          ItemPlan<TestReport> testReportPlan,
                                          FutureReferenceCallbackRegistry callbackRegistry) {
      for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
         Property input = inputs.get(inputIndex);
         if (input instanceof ByteArrayProperty byteArrayProperty) {
            planByteArrayInputReference(testRunReport, input, inputIndex, byteArrayProperty, testReportPlan, callbackRegistry);
         }
      }
   }

   private void planByteArrayInputReference(TestRunReport testRunReport,
                                             Property input,
                                             int inputIndex,
                                             ByteArrayProperty byteArrayProperty,
                                             ItemPlan<TestReport> testReportPlan,
                                             FutureReferenceCallbackRegistry callbackRegistry) {
      String existingAttachmentReference = getExistingAttachmentReference(input);
      if (existingAttachmentReference != null) {
         planExistingAttachmentReference(byteArrayProperty, existingAttachmentReference, testReportPlan, callbackRegistry);
         return;
      }
      if (byteArrayProperty.getValue() == null) {
         return;
      }
      planNewAttachmentReference(testRunReport, inputIndex, byteArrayProperty, testReportPlan, callbackRegistry);
   }

   private void planExistingAttachmentReference(ByteArrayProperty byteArrayProperty,
                                                 String existingAttachmentReference,
                                                 ItemPlan<TestReport> testReportPlan,
                                                 FutureReferenceCallbackRegistry callbackRegistry) {
      String attachmentId = extractAttachmentId(existingAttachmentReference);
      if (attachmentId == null || attachmentId.isBlank()) {
         return;
      }
      testReportPlan.addFutureReferencePlan(
            ReferencePlan.forAttachmentId(byteArrayProperty.getName(), INPUT_ATTACHMENT_REFERENCE_TYPE, attachmentId),
            (testReport, refName, refValue) -> callbackRegistry.notifyResolved(refName, refValue)
      );
   }

   private void planNewAttachmentReference(TestRunReport testRunReport,
                                            int inputIndex,
                                            ByteArrayProperty byteArrayProperty,
                                            ItemPlan<TestReport> testReportPlan,
                                            FutureReferenceCallbackRegistry callbackRegistry) {
      AttachmentPlan attachmentPlan = new AttachmentPlan(
            resolveAttachmentContentType(byteArrayProperty),
            byteArrayProperty.getValue(),
            resolveAttachmentFilename(byteArrayProperty)
      );
      final int finalInputIndex = inputIndex;
      testReportPlan.addFutureReferencePlan(
            ReferencePlan.forAttachment(byteArrayProperty.getName(), INPUT_ATTACHMENT_REFERENCE_TYPE, attachmentPlan),
            (testReport, refName, refId) -> {
               List<Property> updatedInputs = new ArrayList<>(testRunReport.getInputs());
               ByteArrayItemProperty itemProperty = new ByteArrayItemProperty(byteArrayProperty)
                     .setReference(constructPathToAttachment(refId));
               itemProperty.setValue(null);
               if (finalInputIndex < updatedInputs.size()) {
                  updatedInputs.set(finalInputIndex, itemProperty);
               }
               testRunReport.setInputs(updatedInputs);
               callbackRegistry.notifyResolved(refName, refId);
            }
      );
   }

   private void planStepOutputReferences(List<TestRunReport> testRunReports,
                                         ItemPlan<TestReport> testReportPlan,
                                         Map<TestRunReport, FutureReferenceCallbackRegistry> runScopedRegistries) {
      if (testRunReports == null) {
         return;
      }
      for (TestRunReport testRunReport : testRunReports) {
         FutureReferenceCallbackRegistry callbackRegistry = runScopedRegistries.get(testRunReport);
         if (callbackRegistry != null) {
            List<StepRunReport> stepRunReports = testRunReport.getStepRunReports();
            if (stepRunReports != null) {
               planStepOutputReferencesForRun(stepRunReports, testReportPlan, callbackRegistry);
            }
         }
      }
   }

   private void planStepOutputReferencesForRun(List<StepRunReport> stepRunReports,
                                               ItemPlan<TestReport> testReportPlan,
                                               FutureReferenceCallbackRegistry callbackRegistry) {
      for (StepRunReport stepRunReport : stepRunReports) {
         StepOutputPlanDissector dissector = dissectorProvider.getDissector(stepRunReport);
         if (dissector != null) {
            safeDissect(dissector, new StepOutputPlan(testReportPlan, stepRunReport, datahouseUrl, callbackRegistry));
         }
      }
   }

   private void planSubReportReferences(TestReport report,
                                        List<TestReportReferenceDTO> subReportRefs,
                                        ItemPlan<TestReport> testReportPlan,
                                        AccessControlList acl) {
      if (report.getSubReports() == null) {
         return;
      }
      for (TestReport subReport : report.getSubReports()) {
         ItemPlan<TestReport> subReportPlan = buildTestReportPlan(subReport, acl);
         testReportPlan.addFutureReferencePlan(
               ReferencePlan.forItem(subReport.getTestSuiteName(), subReportPlan.type(), subReportPlan),
               (testReport, refName, refId) -> {
                  TestReportReferenceDTO ref = new TestReportReferenceDTO();
                  ref.setTestReportId(refId);
                  ref.setResult(subReport.getResult());
                  ref.setTestSuiteName(subReport.getTestSuiteName());
                  subReportRefs.add(ref);
               }
         );
      }
   }

   private void safeDissect(StepOutputPlanDissector dissector, StepOutputPlan stepOutputPlan) {
      try{
         dissector.dissect(stepOutputPlan);
      }
      catch (Exception e) {
         throw new TestReportRecordingException("Failed to dissect step output for recording, this should be handled properly", e);
      }
   }

   private String constructUrlToItem(String itemId) {
      return datahouseUrl + "/items/" + itemId;
   }

   private String constructPathToAttachment(String attachmentId) {
      return ATTACHMENTS_PATH + attachmentId;
   }

   private Map<TestRunReport, FutureReferenceCallbackRegistry> buildRunScopedRegistries(
         List<TestRunReport> testRunReports,
         FutureReferenceCallbackRegistry callbackRegistry) {
      Map<TestRunReport, FutureReferenceCallbackRegistry> registries = new IdentityHashMap<>();
      if (testRunReports == null) {
         return registries;
      }
      for (int index = 0; index < testRunReports.size(); index++) {
         TestRunReport testRunReport = testRunReports.get(index);
         registries.put(
               testRunReport,
               new NamespacedFutureReferenceCallbackRegistry(callbackRegistry, "run-" + index)
         );
      }
      return registries;
   }

   private static String getExistingAttachmentReference(Property property) {
      if (property instanceof ByteArrayItemProperty byteArrayItemProperty) {
         String reference = byteArrayItemProperty.getReference();
         if (reference != null && !reference.isBlank()) {
            return reference;
         }
      }
      return null;
   }

   private static String extractAttachmentId(String referenceValue) {
      if (referenceValue == null || referenceValue.isBlank()) {
         return null;
      }
      int marker = referenceValue.indexOf(ATTACHMENTS_PATH);
      if (marker < 0) {
         return null;
      }
      String remainder = referenceValue.substring(marker + ATTACHMENTS_PATH.length());
      int endIndex = findEndOfAttachmentId(remainder);
      String attachmentId = endIndex >= 0 ? remainder.substring(0, endIndex) : remainder;
      if (attachmentId.isBlank()) {
         return null;
      }
      return attachmentId;
   }

   private static int findEndOfAttachmentId(String remainder) {
      int slashIndex = remainder.indexOf('/');
      int queryIndex = remainder.indexOf('?');
      int fragmentIndex = remainder.indexOf('#');
      int endIndex = -1;
      if (slashIndex >= 0) {
         endIndex = slashIndex;
      }
      if (queryIndex >= 0 && (endIndex < 0 || queryIndex < endIndex)) {
         endIndex = queryIndex;
      }
      if (fragmentIndex >= 0 && (endIndex < 0 || fragmentIndex < endIndex)) {
         endIndex = fragmentIndex;
      }
      return endIndex;
   }

   private static String resolveAttachmentFilename(ByteArrayProperty byteArrayProperty) {
      if (byteArrayProperty.getFileName() != null && !byteArrayProperty.getFileName().isBlank()) {
         return byteArrayProperty.getFileName();
      }
      return byteArrayProperty.getName();
   }

   private static String resolveAttachmentContentType(ByteArrayProperty byteArrayProperty) {
      if (byteArrayProperty.getMimeType() != null && !byteArrayProperty.getMimeType().isBlank()) {
         return byteArrayProperty.getMimeType();
      }
      return DEFAULT_ATTACHMENT_MIME_TYPE;
   }

   private static final class TestReportItemMarshaller implements TextMarshaller<TestReport> {

      private final List<TestReportReferenceDTO> subReportRefs;
      private final TextSerDes serDes;

      private TestReportItemMarshaller(List<TestReportReferenceDTO> subReportRefs, TextSerDes serDes) {
         this.subReportRefs = subReportRefs;
         this.serDes = serDes;
      }

      @Override
      public String marshallAsString(TestReport report) throws MarshallingException {
         TestReportItemDTO dto = new TestReportItemDTO(report);
         dto.setSubReportsReferences(subReportRefs);
         return serDes.serializeAsString(dto);
      }

      @Override
      public TestReport unmarshall(String payload) {
         throw new UnsupportedOperationException("TestReport marshalling is one-way for recording");
      }

      @Override
      public byte[] marshall(TestReport report) throws MarshallingException {
         return marshallAsString(report).getBytes(StandardCharsets.UTF_8);
      }

      @Override
      public TestReport unmarshall(byte[] payload) {
         throw new UnsupportedOperationException("TestReport marshalling is one-way for recording");
      }
   }
}
