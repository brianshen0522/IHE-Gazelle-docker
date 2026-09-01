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

import net.ihe.gazelle.maestro.api.business.property.ByteArrayItemProperty;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.PropertyBindingPayload;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.simulation.step.business.SimulationStepDefinition;
import net.ihe.gazelle.maestro.spi.business.recording.FutureReferenceCallbackRegistry;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.AttachmentPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.FutureReferencePlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.MaestroRefType;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.simulation.business.callback.*;
import net.ihe.gazelle.simulation.jaxrs.api.technical.dto.callback.SimulationReportDTO;
import net.ihe.gazelle.validation.v2.api.business.Input;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class SimulationStepOutputDissectorTest {

   private static final TextSerDes SER_DES = new JacksonSerDes();
   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
   private final SimulationStepOutputDissector dissector = new SimulationStepOutputDissector();

   @Test
   void shouldPersistValidationReportAsSubItemAndSynthesizeInputsFromBindings() {
      net.ihe.gazelle.validation.v2.api.business.report.ValidationReport validationReportV2 =
            new net.ihe.gazelle.validation.v2.api.business.report.ValidationReport()
                  .addInput(new Input().setId("input-1").setContent("data".getBytes()));
      byte[] validationContent = SER_DES.serializeAsString(
            new net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO(validationReportV2))
            .getBytes(StandardCharsets.UTF_8);

      ValidationReport simValidation = new ValidationReport()
            .setValidationProfileId("profile")
            .setValidationResult(ValidationResult.PASSED)
            .setContent(validationContent);

      TransactionReport transactionReport = new TransactionReport()
            .setTransaction("iti-42")
            .setResult(Result.DONE)
            .setInitiator(new Role().setName("init"))
            .setResponder(new Role().setName("resp"))
            .setValidationReports(List.of(simValidation));
      SimulationReport simulationReport = new SimulationReport()
            .setDateTime(Instant.now())
            .setResult(Result.DONE)
            .setTransactionReports(List.of(transactionReport));

      StepRunReport step = new StepRunReport()
            .addOutput(new ByteArrayProperty(SimulationStepDefinition.REPORT, simulationBytes(simulationReport)))
            .addOutput(new StringProperty(PropertyBindingPayload.PROPERTY_NAME,
                  "{\"schemaVersion\":1,\"stepType\":\"SIMULATION\",\"bindings\":[{\"targetKind\":\"simulationParameter\",\"targetId\":\"input-1\",\"referenceName\":\"inputFile1\",\"propertyName\":\"p-file\"}]}"));
      Fixture fixture = newFixture(step);

      dissector.dissect(fixture.stepOutputPlan());

      assertEquals(1, fixture.rootPlan().futureReferencePlans().size());
      @SuppressWarnings("unchecked")
      ItemPlan<SimulationReport> simulationPlan =
            (ItemPlan<SimulationReport>) fixture.rootPlan().futureReferencePlans().getFirst().referencePlan().getTarget();

      assertEquals(1, simulationPlan.futureReferencePlans().size());
      FutureReferencePlan<SimulationReport> validationRef = simulationPlan.futureReferencePlans().getFirst();
      @SuppressWarnings("unchecked")
      ItemPlan<net.ihe.gazelle.validation.v2.api.business.report.ValidationReport> validationPlan =
            (ItemPlan<net.ihe.gazelle.validation.v2.api.business.report.ValidationReport>)
                  validationRef.referencePlan().getTarget();

      assertEquals(1, validationPlan.futureReferencePlans().size());
      FutureReferencePlan<net.ihe.gazelle.validation.v2.api.business.report.ValidationReport> attachmentReference =
            validationPlan.futureReferencePlans().getFirst();
      AttachmentPlan attachmentPlan = assertInstanceOf(AttachmentPlan.class, attachmentReference.referencePlan().getTarget());
      assertEquals("input-1", attachmentReference.referencePlan().getName());
      assertEquals(MaestroRefType.ATTACHMENT, attachmentReference.referencePlan().getRefType());
      assertArrayEquals("data".getBytes(StandardCharsets.UTF_8), attachmentPlan.content());
      assertNotNull(validationPlan.itemObject().getInputs());
      assertEquals(1, validationPlan.itemObject().getInputs().size());
      assertEquals("input-1", validationPlan.itemObject().getInputs().getFirst().getId());
      fixture.callbackRegistry().notifyResolved("inputFile1", "att-1");
      assertNull(validationPlan.itemObject().getInputs().getFirst().getItemId());
      assertNull(validationPlan.itemObject().getInputs().getFirst().getLocation());
      attachmentReference.mutation().mutateReference(validationPlan.itemObject(), "input-1", "parsed-att-1");
      assertEquals("parsed-att-1", validationPlan.itemObject().getInputs().getFirst().getItemId());
      assertEquals("/attachments/parsed-att-1", validationPlan.itemObject().getInputs().getFirst().getLocation());

      validationRef.mutation().mutateReference(simulationReport, validationRef.referencePlan().getName(), "itm-2");
      ValidationReport mutatedSimValidation = simulationReport.getTransactionReports().getFirst()
            .getValidationReports().getFirst();
      assertNull(mutatedSimValidation.getContent());
      assertEquals("http://dh/items/itm-2", mutatedSimValidation.getReference());
   }

   @Test
   void shouldSkipValidationReportWhenOnlyReferenceProvided() {
      ValidationReport simValidation = new ValidationReport()
            .setValidationResult(ValidationResult.PASSED)
            .setReference("http://existing");

      TransactionReport tx = new TransactionReport()
            .setResult(Result.DONE)
            .setInitiator(new Role().setName("init"))
            .setResponder(new Role().setName("resp"))
            .setValidationReports(List.of(simValidation));
      SimulationReport simulationReport = new SimulationReport()
            .setDateTime(Instant.now())
            .setResult(Result.DONE)
            .setTransactionReports(List.of(tx));

      StepRunReport step = new StepRunReport()
            .addOutput(new ByteArrayProperty(SimulationStepDefinition.REPORT, simulationBytes(simulationReport)));
      Fixture fixture = newFixture(step);

      dissector.dissect(fixture.stepOutputPlan());

      assertEquals(1, fixture.rootPlan().futureReferencePlans().size(), "Only simulation report should be referenced");
      @SuppressWarnings("unchecked")
      ItemPlan<SimulationReport> simulationPlan =
            (ItemPlan<SimulationReport>) fixture.rootPlan().futureReferencePlans().getFirst().referencePlan().getTarget();
      assertTrue(simulationPlan.futureReferencePlans().isEmpty());
   }

   @Test
   void shouldRaiseUnexpectedErrorWhenValidationReportHasNoContentAndNoReference() {
      ValidationReport simValidation = new ValidationReport()
            .setValidationResult(ValidationResult.PASSED);

      TransactionReport tx = new TransactionReport()
            .setResult(Result.DONE)
            .setInitiator(new Role().setName("init"))
            .setResponder(new Role().setName("resp"))
            .setValidationReports(List.of(simValidation));
      SimulationReport simulationReport = new SimulationReport()
            .setDateTime(Instant.now())
            .setResult(Result.DONE)
            .setTransactionReports(List.of(tx));

      StepRunReport step = new StepRunReport()
            .addOutput(new ByteArrayProperty(SimulationStepDefinition.REPORT, simulationBytes(simulationReport)));
      Fixture fixture = newFixture(step);
      StepOutputPlan plan = fixture.stepOutputPlan();

      assertThrows(IllegalStateException.class, () -> dissector.dissect(plan));
   }

   @Test
   void shouldReplaceOutputWithItemReferenceWhenPersisted() {
      SimulationReport simulationReport = new SimulationReport()
            .setDateTime(Instant.now())
            .setResult(Result.DONE)
            .setTransactionReports(List.of());

      StepRunReport step = new StepRunReport()
            .addOutput(new ByteArrayProperty(SimulationStepDefinition.REPORT, simulationBytes(simulationReport)));
      Fixture fixture = newFixture(step);

      dissector.dissect(fixture.stepOutputPlan());
      FutureReferencePlan<TestReport> reportReference = fixture.rootPlan().futureReferencePlans().getFirst();
      reportReference.mutation().mutateReference(new TestReport(), reportReference.referencePlan().getName(), "itm-1");

      ByteArrayItemProperty output = (ByteArrayItemProperty) step.getOutputs().getFirst();
      assertEquals("http://dh/items/itm-1", output.getReference());
      assertNull(output.getValue());
      assertEquals("SIMULATION_REPORT", output.getItemType());
      assertNull(step.getOutput(PropertyBindingPayload.PROPERTY_NAME));
   }

   @Test
   void shouldLinkNestedValidationInputsFromResolvedTestRunAttachments() throws Exception {
      net.ihe.gazelle.validation.v2.api.business.report.ValidationReport validationReportV2 =
            new net.ihe.gazelle.validation.v2.api.business.report.ValidationReport();
      byte[] validationContent = SER_DES.serializeAsString(
            new net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO(validationReportV2))
            .getBytes(StandardCharsets.UTF_8);

      ValidationReport simValidation = new ValidationReport()
            .setValidationProfileId("profile")
            .setValidationResult(ValidationResult.PASSED)
            .setContent(validationContent);
      TransactionReport transactionReport = new TransactionReport()
            .setTransaction("iti-42")
            .setResult(Result.DONE)
            .setInitiator(new Role().setName("init"))
            .setResponder(new Role().setName("resp"))
            .setValidationReports(List.of(simValidation));
      SimulationReport simulationReport = new SimulationReport()
            .setDateTime(Instant.now())
            .setResult(Result.DONE)
            .setTransactionReports(List.of(transactionReport));

      String propertyBinding = OBJECT_MAPPER.writeValueAsString(new PropertyBindingPayload()
            .setStepType("SIMULATION")
            .addBinding(new PropertyBindingPayload.Binding()
                  .setTargetKind("simulationParameter")
                  .setTargetId("input-1")
                  .setReferenceName("inputFile1")
                  .setPropertyName("p-file")));

      StepRunReport step = new StepRunReport()
            .addOutput(new ByteArrayProperty(SimulationStepDefinition.REPORT, simulationBytes(simulationReport)))
            .addOutput(new StringProperty(PropertyBindingPayload.PROPERTY_NAME, propertyBinding));
      Fixture fixture = newFixture(step);

      dissector.dissect(fixture.stepOutputPlan());
      @SuppressWarnings("unchecked")
      ItemPlan<SimulationReport> simulationPlan =
            (ItemPlan<SimulationReport>) fixture.rootPlan().futureReferencePlans().getFirst().referencePlan().getTarget();
      @SuppressWarnings("unchecked")
      ItemPlan<net.ihe.gazelle.validation.v2.api.business.report.ValidationReport> validationPlan =
            (ItemPlan<net.ihe.gazelle.validation.v2.api.business.report.ValidationReport>)
                  simulationPlan.futureReferencePlans().getFirst().referencePlan().getTarget();

      fixture.callbackRegistry().notifyResolved("inputFile1", "att-77");
      net.ihe.gazelle.validation.v2.api.business.report.ValidationReport mutated = validationPlan.itemObject();
      assertNotNull(mutated.getInputs());
      assertEquals(1, mutated.getInputs().size());
      assertEquals("input-1", mutated.getInputs().getFirst().getId());
      assertEquals("att-77", mutated.getInputs().getFirst().getItemId());
      assertEquals("/attachments/att-77", mutated.getInputs().getFirst().getLocation());
      assertTrue(validationPlan.futureReferencePlans().stream()
            .map(FutureReferencePlan::referencePlan)
            .anyMatch(referencePlan ->
                  "input-1".equals(referencePlan.getName())
                        && referencePlan.getRefType() == MaestroRefType.ATTACHMENT
                        && "att-77".equals(referencePlan.getTarget())));
   }

   private Fixture newFixture(StepRunReport stepRunReport) {
      ItemPlan<TestReport> rootPlan = new ItemPlan<>("TEST_REPORT", new TestReport(), new TestReportMarshaller());
      TestFutureReferenceCallbackRegistry callbackRegistry = new TestFutureReferenceCallbackRegistry();
      StepOutputPlan stepOutputPlan = new StepOutputPlan(rootPlan, stepRunReport, "http://dh", callbackRegistry);
      return new Fixture(rootPlan, stepOutputPlan, callbackRegistry);
   }

   private byte[] simulationBytes(SimulationReport simulationReport) {
      return SER_DES.serializeAsString(new SimulationReportDTO(simulationReport)).getBytes(StandardCharsets.UTF_8);
   }

   private record Fixture(ItemPlan<TestReport> rootPlan,
                          StepOutputPlan stepOutputPlan,
                          TestFutureReferenceCallbackRegistry callbackRegistry) {
   }

   private static final class TestReportMarshaller implements TextMarshaller<TestReport> {
      @Override
      public String marshallAsString(TestReport report) throws MarshallingException {
         return "{}";
      }

      @Override
      public TestReport unmarshall(String payload) {
         throw new UnsupportedOperationException();
      }

      @Override
      public byte[] marshall(TestReport report) throws MarshallingException {
         return marshallAsString(report).getBytes(StandardCharsets.UTF_8);
      }

      @Override
      public TestReport unmarshall(byte[] payload) {
         throw new UnsupportedOperationException();
      }
   }

   private static final class TestFutureReferenceCallbackRegistry implements FutureReferenceCallbackRegistry {
      private final java.util.Map<String, java.util.List<java.util.function.BiConsumer<String, String>>> callbacksByReferenceName =
            new java.util.HashMap<>();

      @Override
      public void registerCallback(String referenceName, java.util.function.BiConsumer<String, String> callback) {
         callbacksByReferenceName.computeIfAbsent(referenceName, key -> new java.util.ArrayList<>()).add(callback);
      }

      @Override
      public void notifyResolved(String referenceName, String referenceId) {
         java.util.List<java.util.function.BiConsumer<String, String>> callbacks = callbacksByReferenceName.get(referenceName);
         if (callbacks == null) {
            return;
         }
         for (java.util.function.BiConsumer<String, String> callback : callbacks) {
            callback.accept(referenceName, referenceId);
         }
      }
   }
}
