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

package net.ihe.gazelle.maestro.spi.business.recording;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayItemProperty;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.testreport.Result;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.spi.business.recording.plan.AttachmentPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.FutureReferencePlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ReferencePlan;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class RecordingPlanTest {

   private static final TextMarshaller<String> STRING_MARSHALLER = new TextMarshaller<>() {
      @Override
      public String marshallAsString(String value) throws MarshallingException {
         return value;
      }

      @Override
      public String unmarshall(String payload) {
         return payload;
      }

      @Override
      public byte[] marshall(String value) throws MarshallingException {
         return value.getBytes(StandardCharsets.UTF_8);
      }

      @Override
      public String unmarshall(byte[] payload) {
         return new String(payload, StandardCharsets.UTF_8);
      }
   };

   @Test
   void forItemKeepsProvidedTypeAndTarget() {
      ItemPlan<String> childPlan = new ItemPlan<>("child-type", "child", STRING_MARSHALLER);

      ReferencePlan referencePlan = ReferencePlan.forItem("child-ref", childPlan.type(), childPlan);

      assertEquals("child-type", referencePlan.getTargetType());
      assertSame(childPlan, referencePlan.getTarget());
   }

   @Test
   void withItemReferenceUsesReferencedItemType() {
      ItemPlan<String> parentPlan = new ItemPlan<>("parent-type", "parent", STRING_MARSHALLER);
      ItemPlan<String> childPlan = new ItemPlan<>("child-type", "child", STRING_MARSHALLER);

      parentPlan.withItemReference("child-ref", childPlan);
      FutureReferencePlan<String> futureReferencePlan = parentPlan.futureReferencePlans().getFirst();

      assertEquals("child-type", futureReferencePlan.referencePlan().getTargetType());
      assertSame(childPlan, futureReferencePlan.referencePlan().getTarget());
   }

   @Test
   void addItemReferencePlanUsesItemType() {
      ItemPlan<TestReport> rootReportPlan = new ItemPlan<>("test-report", newTestReport(), null);
      StepRunReport stepRunReport = new StepRunReport()
            .addOutput(new ByteArrayProperty("output-a", "value".getBytes(StandardCharsets.UTF_8)));
      StepOutputPlan stepOutputPlan = new StepOutputPlan(
            rootReportPlan,
            stepRunReport,
            "http://dh",
            new NoOpFutureReferenceCallbackRegistry()
      );
      ItemPlan<String> outputPlan = new ItemPlan<>("output-type", "output-payload", STRING_MARSHALLER);

      stepOutputPlan.addItemReferencePlan("output-a", outputPlan);
      FutureReferencePlan<TestReport> futureReferencePlan = rootReportPlan.futureReferencePlans().getFirst();

      assertEquals("output-type", futureReferencePlan.referencePlan().getTargetType());
      assertSame(outputPlan, futureReferencePlan.referencePlan().getTarget());
   }

   @Test
   void replaceOutputReturnsFalseForInvalidInput() {
      StepRunReport stepRunReport = new StepRunReport();

      assertFalse(StepOutputPlan.replaceOutput(null, property -> property, stepRunReport));
      assertFalse(StepOutputPlan.replaceOutput("out", null, stepRunReport));
      assertFalse(StepOutputPlan.replaceOutput("out", property -> property, null));
   }

   @Test
   void addAttachmentReferencePlanReplacesByteArrayOutput() {
      ItemPlan<TestReport> rootReportPlan = new ItemPlan<>("test-report", newTestReport(), null);
      StepRunReport stepRunReport = new StepRunReport()
            .addOutput(new ByteArrayProperty("output-a", "value".getBytes(StandardCharsets.UTF_8)));
      StepOutputPlan stepOutputPlan = new StepOutputPlan(
            rootReportPlan,
            stepRunReport,
            "http://dh",
            new NoOpFutureReferenceCallbackRegistry()
      );
      AttachmentPlan attachmentPlan = new AttachmentPlan("application/pdf", "abc".getBytes(StandardCharsets.UTF_8), "report.pdf");

      stepOutputPlan.addAttachmentReferencePlan("output-a", attachmentPlan);
      FutureReferencePlan<TestReport> futureReferencePlan = rootReportPlan.futureReferencePlans().getFirst();
      futureReferencePlan.mutation().mutateReference(null, "output-a", "att-1");

      ByteArrayItemProperty output = stepRunReport.getOutput("output-a");
      assertEquals("/attachments/att-1", output.getReference());
      assertEquals("application/pdf", futureReferencePlan.referencePlan().getTargetType());
      assertSame(attachmentPlan, futureReferencePlan.referencePlan().getTarget());
   }

   @Test
   void addAttachmentReferencePlanNotifiesCallbackWhenResolved() {
      ItemPlan<TestReport> rootReportPlan = new ItemPlan<>("test-report", newTestReport(), null);
      StepRunReport stepRunReport = new StepRunReport()
            .addOutput(new ByteArrayProperty("output-a", "value".getBytes(StandardCharsets.UTF_8)));
      TestFutureReferenceCallbackRegistry callbackRegistry = new TestFutureReferenceCallbackRegistry();
      StepOutputPlan stepOutputPlan = new StepOutputPlan(
            rootReportPlan,
            stepRunReport,
            "http://dh",
            callbackRegistry
      );
      AtomicReference<String> resolvedReferenceId = new AtomicReference<>();
      stepOutputPlan.onFutureReferenceResolved("output-a", (referenceName, referenceId) -> resolvedReferenceId.set(referenceId));

      stepOutputPlan.addAttachmentReferencePlan(
            "output-a",
            new AttachmentPlan("application/pdf", "abc".getBytes(StandardCharsets.UTF_8), "report.pdf")
      );
      FutureReferencePlan<TestReport> futureReferencePlan = rootReportPlan.futureReferencePlans().getFirst();
      futureReferencePlan.mutation().mutateReference(null, "output-a", "att-1");

      assertEquals("att-1", resolvedReferenceId.get());
   }

   @Test
   void addItemReferencePlanDoesNotNotifyCallbackWhenResolved() {
      ItemPlan<TestReport> rootReportPlan = new ItemPlan<>("test-report", newTestReport(), null);
      StepRunReport stepRunReport = new StepRunReport()
            .addOutput(new ByteArrayProperty("output-a", "value".getBytes(StandardCharsets.UTF_8)));
      TestFutureReferenceCallbackRegistry callbackRegistry = new TestFutureReferenceCallbackRegistry();
      StepOutputPlan stepOutputPlan = new StepOutputPlan(
            rootReportPlan,
            stepRunReport,
            "http://dh",
            callbackRegistry
      );
      AtomicReference<String> resolvedReferenceId = new AtomicReference<>();
      stepOutputPlan.onFutureReferenceResolved("output-a", (referenceName, referenceId) -> resolvedReferenceId.set(referenceId));

      stepOutputPlan.addItemReferencePlan(
            "output-a",
            new ItemPlan<>("output-type", "output-payload", STRING_MARSHALLER)
      );
      FutureReferencePlan<TestReport> futureReferencePlan = rootReportPlan.futureReferencePlans().getFirst();
      futureReferencePlan.mutation().mutateReference(null, "output-a", "itm-1");

      assertNull(resolvedReferenceId.get());
   }

   @Test
   void addAdditionalParametersMergesParameters() {
      ItemPlan<String> itemPlan = new ItemPlan<>("type", "payload", STRING_MARSHALLER);
      itemPlan.addAdditionalParameter("a", "1");
      itemPlan.addAdditionalParameters(Map.of("b", "2"));
      Map<String, String> copy = itemPlan.additionalParameters();
      copy.put("c", "3");

      assertEquals("1", itemPlan.additionalParameters().get("a"));
      assertEquals("2", itemPlan.additionalParameters().get("b"));
      assertFalse(itemPlan.additionalParameters().containsKey("c"));
   }

   @Test
   void replaceOutputUpdatesMatchingOutputOnly() {
      StepRunReport stepRunReport = new StepRunReport()
            .addOutput(new StringProperty("first", "value-1"))
            .addOutput(new StringProperty("second", "value-2"));

      boolean replaced = StepOutputPlan.replaceOutput(
            "second",
            property -> new StringProperty(property.getName(), "value-2-updated"),
            stepRunReport
      );

      assertTrue(replaced);
      assertEquals("value-1", ((StringProperty) stepRunReport.getOutput("first")).getValue());
      assertEquals("value-2-updated", ((StringProperty) stepRunReport.getOutput("second")).getValue());
   }

   private static TestReport newTestReport() {
      return new TestReport()
            .setUuid("report-1")
            .setDateTime(Instant.now())
            .setResult(Result.UNDEFINED);
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

   private static final class TestFutureReferenceCallbackRegistry implements FutureReferenceCallbackRegistry {
      private final Map<String, List<BiConsumer<String, String>>> callbacksByReferenceName = new HashMap<>();

      @Override
      public void registerCallback(String referenceName, BiConsumer<String, String> callback) {
         callbacksByReferenceName.computeIfAbsent(referenceName, key -> new ArrayList<>()).add(callback);
      }

      @Override
      public void notifyResolved(String referenceName, String referenceId) {
         List<BiConsumer<String, String>> callbacks = callbacksByReferenceName.get(referenceName);
         if (callbacks == null) {
            return;
         }
         for (BiConsumer<String, String> callback : callbacks) {
            callback.accept(referenceName, referenceId);
         }
      }
   }
}
