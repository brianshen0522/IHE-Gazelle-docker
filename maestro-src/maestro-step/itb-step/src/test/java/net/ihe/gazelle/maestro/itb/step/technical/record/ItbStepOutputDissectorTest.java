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

package net.ihe.gazelle.maestro.itb.step.technical.record;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayItemProperty;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.itb.step.business.ItbStepDefinition;
import net.ihe.gazelle.maestro.spi.business.recording.FutureReferenceCallbackRegistry;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.AttachmentPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.FutureReferencePlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.MaestroRefType;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItbStepOutputDissectorTest {

   private static final TextMarshaller<TestReport> TEST_REPORT_MARSHALLER = new TextMarshaller<>() {

      @Override
      public byte[] marshall(TestReport report) throws MarshallingException {
         return marshallAsString(report).getBytes(StandardCharsets.UTF_8);
      }

      @Override
      public TestReport unmarshall(byte[] payload) {
         throw new UnsupportedOperationException();
      }
   };

   private final ItbStepOutputDissector dissector = new ItbStepOutputDissector();

   @Test
   void shouldCreateAttachmentReferencesForXmlPdfAndLogsOutputs() {
      StepRunReport stepRunReport = new StepRunReport()
            .addOutput(new ByteArrayProperty(ItbStepDefinition.XML_REPORT, "<xml/>".getBytes(StandardCharsets.UTF_8))
                  .setFileName("report.xml"))
            .addOutput(new ByteArrayProperty(ItbStepDefinition.PDF_REPORT, "pdf".getBytes(StandardCharsets.UTF_8))
                  .setFileName("report.pdf"))
            .addOutput(new ByteArrayProperty(ItbStepDefinition.LOGS, "logs".getBytes(StandardCharsets.UTF_8))
                  .setFileName("logs.txt"));

      ItemPlan<TestReport> rootPlan = new ItemPlan<>("TEST_REPORT", new TestReport(), TEST_REPORT_MARSHALLER);
      StepOutputPlan stepOutputPlan = new StepOutputPlan(rootPlan, stepRunReport, "http://dh",
            new NoOpFutureReferenceCallbackRegistry());

      dissector.dissect(stepOutputPlan);

      List<FutureReferencePlan<TestReport>> references = rootPlan.futureReferencePlans();
      assertEquals(3, references.size());
      for (FutureReferencePlan<TestReport> reference : references) {
         assertEquals(MaestroRefType.ATTACHMENT, reference.referencePlan().getRefType());
         AttachmentPlan attachmentPlan = (AttachmentPlan) reference.referencePlan().getTarget();
         assertEquals(ItbStepOutputDissector.BYTE_ARRAY, attachmentPlan.type());
      }
   }

   @Test
   void shouldReplaceXmlOutputWithAttachmentReferenceWhenMutationIsApplied() {
      StepRunReport stepRunReport = new StepRunReport()
            .addOutput(new ByteArrayProperty(ItbStepDefinition.XML_REPORT, "<xml/>".getBytes(StandardCharsets.UTF_8))
                  .setFileName("report.xml"));

      ItemPlan<TestReport> rootPlan = new ItemPlan<>("TEST_REPORT", new TestReport(), TEST_REPORT_MARSHALLER);
      StepOutputPlan stepOutputPlan = new StepOutputPlan(rootPlan, stepRunReport, "http://dh",
            new NoOpFutureReferenceCallbackRegistry());

      dissector.dissect(stepOutputPlan);
      FutureReferencePlan<TestReport> xmlReference = rootPlan.futureReferencePlans().getFirst();
      xmlReference.mutation().mutateReference(new TestReport(), ItbStepDefinition.XML_REPORT, "att-1");

      ByteArrayItemProperty mutatedOutput = (ByteArrayItemProperty) stepRunReport.getOutputs().getFirst();
      assertEquals("/attachments/att-1", mutatedOutput.getReference());
      assertNull(mutatedOutput.getValue());
      assertEquals("report.xml", mutatedOutput.getFileName());
   }

   @Test
   void shouldNotCreateAnyReferenceWhenNoSupportedOutputsArePresent() {
      StepRunReport stepRunReport = new StepRunReport();

      ItemPlan<TestReport> rootPlan = new ItemPlan<>("TEST_REPORT", new TestReport(), TEST_REPORT_MARSHALLER);
      StepOutputPlan stepOutputPlan = new StepOutputPlan(rootPlan, stepRunReport, "http://dh",
            new NoOpFutureReferenceCallbackRegistry());

      dissector.dissect(stepOutputPlan);

      assertTrue(rootPlan.futureReferencePlans().isEmpty());
   }
   private static final class NoOpFutureReferenceCallbackRegistry implements FutureReferenceCallbackRegistry {
      @Override
      public void registerCallback(String referenceName, java.util.function.BiConsumer<String, String> callback) {
         // intentionally empty no-op
      }

      @Override
      public void notifyResolved(String referenceName, String referenceId) {
         // intentionally empty no-op
      }
   }
}
