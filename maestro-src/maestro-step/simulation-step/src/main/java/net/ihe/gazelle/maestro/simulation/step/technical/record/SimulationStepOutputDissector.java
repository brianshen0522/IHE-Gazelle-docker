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

package net.ihe.gazelle.maestro.simulation.step.technical.record;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.PropertyBindingPayload;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.simulation.step.business.SimulationStepDefinition;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlan;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissector;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.simulation.business.callback.SimulationReport;
import net.ihe.gazelle.simulation.business.callback.TransactionReport;
import net.ihe.gazelle.simulation.business.callback.ValidationReport;
import net.ihe.gazelle.simulation.jaxrs.api.technical.dto.callback.SimulationReportDTO;
import net.ihe.gazelle.maestro.validation.step.technical.record.ValidationInputReferenceBindingService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Structures the simulation step report into a set of hierarchical Datahouse items.
 */
class SimulationStepOutputDissector implements StepOutputPlanDissector {

   private static final TextSerDes serDes = new JacksonSerDes();
   private static final List<String> ACCEPTED_TARGET_KINDS = List.of("validationInput", "simulationParameter");

   private static final String ITEM_TYPE_SIMULATION_REPORT = "SIMULATION_REPORT";
   private static final String ITEM_TYPE_VALIDATION_REPORT = "VALIDATION_REPORT";
   private static final String VALIDATION_INPUT_TYPE = "VALIDATION_INPUT";
   private static final String VALIDATION_REPORT_REFERENCE_PREFIX = "validation-report:";
   private final ValidationInputReferenceBindingService inputReferenceBindingService;

   SimulationStepOutputDissector() {
      this.inputReferenceBindingService = new ValidationInputReferenceBindingService();
   }

   @Override
   public void dissect(StepOutputPlan stepOutputPlan) {
      if (stepOutputPlan == null || stepOutputPlan.getStepRunReport() == null) {
         return;
      }
      StepRunReport stepRunReport = stepOutputPlan.getStepRunReport();
      PropertyBindingPayload propertyBindingPayload = inputReferenceBindingService.consumePropertyBinding(stepRunReport);
      List<Property> outputs = stepRunReport.getOutputs();
      if (outputs == null || outputs.isEmpty()) {
         return;
      }

      ByteArrayProperty reportProperty = stepRunReport.getOutput(SimulationStepDefinition.REPORT);
      if (reportProperty == null) {
         return;
      }
      SimulationReport simulationReport = deserializeReport(reportProperty);

      ItemPlan<SimulationReport> simulationPlan = new ItemPlan<>(
            ITEM_TYPE_SIMULATION_REPORT,
            simulationReport,
            new SimulationReportMarshaller()
      );

      Map<String, ValidationInputReferenceBindingService.BindingInfo> bindingInfoByTargetId =
            inputReferenceBindingService.toBindingInfoByTargetId(propertyBindingPayload, ACCEPTED_TARGET_KINDS);
      addValidationReportSubItems(
            simulationPlan,
            simulationReport,
            stepOutputPlan,
            stepOutputPlan.getDatahouseUrl(),
            bindingInfoByTargetId
      );

      stepOutputPlan.addItemReferencePlan(SimulationStepDefinition.REPORT, simulationPlan);
   }

   private void addValidationReportSubItems(ItemPlan<SimulationReport> simulationPlan,
                                            SimulationReport simulationReport,
                                            StepOutputPlan stepOutputPlan,
                                            String datahouseUrl,
                                            Map<String, ValidationInputReferenceBindingService.BindingInfo> bindingInfoByTargetId) {
      if (simulationReport.getTransactionReports() == null) {
         return;
      }
      for (TransactionReport transaction : simulationReport.getTransactionReports()) {
         if (transaction.getValidationReports() == null) {
            continue;
         }
         for (ValidationReport validationReport : transaction.getValidationReports()) {
            byte[] validationContent = validationReport.getContent();
            if (validationContent == null || validationContent.length == 0) {
               String existingReference = validationReport.getReference();
               if (existingReference != null && !existingReference.isBlank()) {
                  continue; // skip when only reference is present
               }
               throw new IllegalStateException(
                     "Simulation validation report is missing content and does not provide a reference");
            }
            net.ihe.gazelle.validation.v2.api.business.report.ValidationReport validationReportV2 =
                  deserializeValidationReportV2(validationContent);
            ItemPlan<net.ihe.gazelle.validation.v2.api.business.report.ValidationReport> validationPlan =
                  new ItemPlan<>(
                        ITEM_TYPE_VALIDATION_REPORT,
                        validationReportV2,
                        new ValidationReportMarshaller()
                  );
            inputReferenceBindingService.registerInputPersistence(
                  stepOutputPlan,
                  validationPlan,
                  validationReportV2,
                  bindingInfoByTargetId,
                  VALIDATION_INPUT_TYPE
            );
            String refName = buildValidationReferenceName(transaction, validationReport);
            simulationPlan.withItemReference(
                  refName,
                  validationPlan,
                  (report, referenceName, itemId) ->
                        mutateSimulationValidationReport(report, referenceName, itemId, datahouseUrl)
            );
         }
      }
   }

   private void mutateSimulationValidationReport(SimulationReport report,
                                                 String referenceName,
                                                 String itemId,
                                                 String datahouseUrl) {
      if (report == null || report.getTransactionReports() == null) {
         return;
      }
      for (TransactionReport transaction : report.getTransactionReports()) {
         if (transaction.getValidationReports() == null) {
            continue;
         }
         for (ValidationReport validationReport : transaction.getValidationReports()) {
            String candidate = buildValidationReferenceName(transaction, validationReport);
            if (candidate.equals(referenceName)) {
               validationReport.setReference(datahouseUrl + "/items/" + itemId);
               validationReport.setContent(null);
               return;
            }
         }
      }
   }

   private SimulationReport deserializeReport(ByteArrayProperty reportProperty) {
      byte[] value = reportProperty.getValue();
      if (value == null || value.length == 0) {
         throw new IllegalStateException("Simulation report output is empty");
      }
      return serDes.deserialize(value, SimulationReportDTO.class).getBusinessObject();
   }

   private net.ihe.gazelle.validation.v2.api.business.report.ValidationReport deserializeValidationReportV2(byte[] content) {
      return serDes.deserialize(content, net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO.class)
            .getBusinessObject();
   }

   // Builds a unique reference name in the form:
   // validation-report:<transaction-id>:<validation-subject>:<validation-profile-id>
   private String buildValidationReferenceName(TransactionReport transaction, ValidationReport validationReport) {
      return VALIDATION_REPORT_REFERENCE_PREFIX
            + normalizeReferencePart(transaction == null ? null : transaction.getTransaction()) + ":"
            + normalizeReferencePart(validationReport == null ? null : validationReport.getSubject()) + ":"
            + normalizeReferencePart(validationReport == null ? null : validationReport.getValidationProfileId());
   }

   private String normalizeReferencePart(String value) {
      if (value == null || value.isBlank()) {
         return "unknown";
      }
      return value.trim().replaceAll("\\s+", "_");
   }

   private static final class SimulationReportMarshaller implements TextMarshaller<SimulationReport> {
      @Override
      public String marshallAsString(SimulationReport report) throws MarshallingException {
         return serDes.serializeAsString(new SimulationReportDTO(report));
      }

      @Override
      public SimulationReport unmarshall(String payload) {
         throw new UnsupportedOperationException("SimulationReport marshalling is one-way for recording");
      }

      @Override
      public byte[] marshall(SimulationReport report) throws MarshallingException {
         return marshallAsString(report).getBytes(StandardCharsets.UTF_8);
      }

      @Override
      public SimulationReport unmarshall(byte[] payload) {
         throw new UnsupportedOperationException("SimulationReport marshalling is one-way for recording");
      }
   }

   private static final class ValidationReportMarshaller
         implements TextMarshaller<net.ihe.gazelle.validation.v2.api.business.report.ValidationReport> {
      @Override
      public String marshallAsString(net.ihe.gazelle.validation.v2.api.business.report.ValidationReport report)
            throws MarshallingException {
         return serDes.serializeAsString(
               new net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO(report));
      }

      @Override
      public net.ihe.gazelle.validation.v2.api.business.report.ValidationReport unmarshall(String payload) {
         throw new UnsupportedOperationException("ValidationReport marshalling is one-way for recording");
      }

      @Override
      public byte[] marshall(net.ihe.gazelle.validation.v2.api.business.report.ValidationReport report)
            throws MarshallingException {
         return marshallAsString(report).getBytes(StandardCharsets.UTF_8);
      }

      @Override
      public net.ihe.gazelle.validation.v2.api.business.report.ValidationReport unmarshall(byte[] payload) {
         throw new UnsupportedOperationException("ValidationReport marshalling is one-way for recording");
      }
   }
}
