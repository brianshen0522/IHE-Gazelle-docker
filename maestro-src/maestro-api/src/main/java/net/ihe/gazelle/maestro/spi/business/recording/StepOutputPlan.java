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
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.spi.business.recording.plan.AttachmentPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemObjectMutation;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.MaestroRefType;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ReferencePlan;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * Mutable plan used to transform step outputs into item/attachment references before persistence.
 */
public class StepOutputPlan {

   private final BiFunction<ReferencePlan, ItemObjectMutation<TestReport>, ItemPlan<TestReport>> addReferencePlanFunction;

   private final StepRunReport stepRunReport;

   private final String datahouseUrl;
   private final FutureReferenceCallbackRegistry callbackRegistry;

   /**
    * Creates a new output plan for a single step execution.
    *
    * @param rootReportPlan root report plan receiving future references
    * @param stepRunReport step run report to mutate
    * @param datahouseUrl base Datahouse URL used to build item references
    * @param callbackRegistry registry used to subscribe to resolved future references
    */
   public StepOutputPlan(ItemPlan<TestReport> rootReportPlan,
                         StepRunReport stepRunReport,
                         String datahouseUrl,
                         FutureReferenceCallbackRegistry callbackRegistry) {
      this.addReferencePlanFunction = rootReportPlan::addFutureReferencePlan;
      this.stepRunReport = stepRunReport;
      this.datahouseUrl = datahouseUrl;
      this.callbackRegistry = callbackRegistry;
   }

   /**
    * Adds a reference plan and optionally mutates the step report once the reference is persisted.
    *
    * @param referencePlan reference definition to add
    * @param mutation optional mutation callback
    * @return current plan
    */
   public StepOutputPlan addReferencePlan(ReferencePlan referencePlan, ItemObjectMutation<StepRunReport> mutation) {
      if (referencePlan == null) {
         throw new IllegalArgumentException("referencePlan must not be null");
      }
      addReferencePlanFunction.apply(referencePlan,
            (testReport, referenceName, referenceId) -> {
               if (mutation != null) {
                  mutation.mutateReference(stepRunReport, referenceName, referenceId);
               }
               notifyResolvedAttachment(referencePlan, referenceName, referenceId);
            });

      return this;
   }

   /**
    * Adds an item reference and attempts to replace a matching byte-array output with a reference output.
    *
    * @param outputName output property name
    * @param itemPlan referenced item plan
    * @return current plan
    */
   public StepOutputPlan addItemReferencePlan(String outputName, ItemPlan<?> itemPlan) {
      if (outputName == null || outputName.isBlank() || itemPlan == null) {
         throw new IllegalArgumentException("outputName and itemPlan must not be null or blank");
      }
      return addReferencePlan(
            ReferencePlan.forItem(outputName, itemPlan.type(), itemPlan),
            (currentStepReport, referenceName, itemId) ->
                  replaceOutput(referenceName,
                        output -> toItemOutputReference(output, itemPlan.type(), datahouseUrl + "/items/" + itemId),
                        currentStepReport)
      );
   }

   /**
    * Adds an attachment reference and attempts to replace a matching byte-array output with a reference output.
    *
    * @param outputName output property name
    * @param attachmentPlan attachment definition to persist
    * @return current plan
    */
   public StepOutputPlan addAttachmentReferencePlan(String outputName, AttachmentPlan attachmentPlan) {
      if (outputName == null || outputName.isBlank() || attachmentPlan == null) {
         throw new IllegalArgumentException("outputName and attachmentPlan must not be null or blank");
      }
      return addReferencePlan(
            ReferencePlan.forAttachment(outputName, attachmentPlan),
            (currentStepReport, referenceName, attachmentId) ->
                  replaceOutput(referenceName,
                        output -> toAttachmentOutputReference(output, "/attachments/" + attachmentId),
                        currentStepReport)
      );
   }

   /**
    * Returns Datahouse base URL used when building references.
    *
    * @return Datahouse base URL
    */
   public String getDatahouseUrl() {
      return datahouseUrl;
   }

   /**
    * Returns the step run report currently being transformed.
    *
    * @return mutable step run report
    */
   public StepRunReport getStepRunReport() {
      return stepRunReport;
   }

   /**
    * Registers a callback to run when a future reference with the given name is resolved.
    *
    * @param referenceName logical reference name
    * @param callback callback invoked with (referenceName, resolvedReferenceId)
    */
   public void onFutureReferenceResolved(String referenceName, BiConsumer<String, String> callback) {
      if (referenceName == null || referenceName.isBlank() || callback == null) {
         throw new IllegalArgumentException("referenceName and callback must not be null or blank");
      }
      callbackRegistry.registerCallback(referenceName, callback);
   }

   /**
    * Replaces one output property in place when found.
    *
    * @param outputName output name to replace
    * @param replacementFactory replacement factory
    * @param stepRunReport target report
    * @return {@code true} if a property was replaced
    */
   public static boolean replaceOutput(String outputName, UnaryOperator<Property> replacementFactory,
                                       StepRunReport stepRunReport) {
      if (stepRunReport == null || stepRunReport.getOutputs() == null || outputName == null
              || replacementFactory == null) {
         return false;
      }
      List<Property> outputs = stepRunReport.getOutputs();
      for (int i = 0; i < outputs.size(); i++) {
         Property output = outputs.get(i);
         if (outputName.equals(output.getName())) {
            outputs.set(i, Objects.requireNonNull(replacementFactory.apply(output), "replacement"));
            stepRunReport.setOutputs(outputs);
            return true;
         }
      }
      return false;
   }

   private static Property toItemOutputReference(Property output, String itemType, String reference) {
      if (output instanceof ByteArrayProperty byteArrayProperty) {
         ByteArrayItemProperty replacement = new ByteArrayItemProperty(byteArrayProperty);
         replacement.setValue(null);
         replacement.setItemType(itemType);
         replacement.setReference(reference);
         return replacement;
      }
      return output;
   }

   private static Property toAttachmentOutputReference(Property output, String reference) {
      if (output instanceof ByteArrayProperty byteArrayProperty) {
         ByteArrayItemProperty replacement = new ByteArrayItemProperty(byteArrayProperty);
         replacement.setValue(null);
         replacement.setReference(reference);
         return replacement;
      }
      return output;
   }

   private void notifyResolvedAttachment(ReferencePlan referencePlan, String referenceName, String referenceId) {
      if (referencePlan.getRefType() == MaestroRefType.ATTACHMENT) {
         callbackRegistry.notifyResolved(referenceName, referenceId);
      }
   }

}
