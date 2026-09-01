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

package net.ihe.gazelle.maestro.itb.step.technical.record;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.itb.step.business.ItbStepDefinition;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlan;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissector;
import net.ihe.gazelle.maestro.spi.business.recording.plan.AttachmentPlan;

/**
 * Dissects ITB outputs for recording by persisting report/log outputs as attachments.
 */
public class ItbStepOutputDissector implements StepOutputPlanDissector {

   static final String BYTE_ARRAY = "BYTE_ARRAY";

   /**
    * Creates an ITB output dissector.
    */
   public ItbStepOutputDissector() {
      // default constructor
   }

   @Override
   public void dissect(StepOutputPlan stepOutputPlan) {
      if (stepOutputPlan == null || stepOutputPlan.getStepRunReport() == null) {
         return;
      }

      StepRunReport stepRunReport = stepOutputPlan.getStepRunReport();
      if (stepRunReport.getOutputs().isEmpty()) {
         return;
      }

      addAttachmentReferenceForOutput(stepOutputPlan, ItbStepDefinition.XML_REPORT);
      addAttachmentReferenceForOutput(stepOutputPlan, ItbStepDefinition.PDF_REPORT);
      addAttachmentReferenceForOutput(stepOutputPlan, ItbStepDefinition.LOGS);
   }

   private void addAttachmentReferenceForOutput(StepOutputPlan stepOutputPlan, String outputName) {
      ByteArrayProperty property = stepOutputPlan.getStepRunReport().getOutput(outputName);
      if (property == null || property.getValue() == null) {
         return;
      }

      AttachmentPlan attachmentPlan = new AttachmentPlan(
            BYTE_ARRAY,
            property.getValue(),
            property.getFileName()
      );

      stepOutputPlan.addAttachmentReferencePlan(outputName, attachmentPlan);
   }
}
