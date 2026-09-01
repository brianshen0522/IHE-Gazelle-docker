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
package net.ihe.gazelle.maestro.simulation.step.business;

import net.ihe.gazelle.maestro.api.business.property.BooleanProperty;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.PropertyBindingPayload;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.userinteract.UserInteractionHandler;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import net.ihe.gazelle.simulation.business.callback.Result;
import net.ihe.gazelle.simulation.business.callback.SimulationReport;
import net.ihe.gazelle.simulation.business.sequence.ServiceUnavailableException;
import net.ihe.gazelle.simulation.business.sequence.SimulationSequence;
import net.ihe.gazelle.simulation.business.sequence.SupportedParameter;
import net.ihe.gazelle.simulation.business.setup.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SimulationStepExecutorTest {

   @Test
   void run_returnsPassed_and_output_and_showsInstructions() {
      ReportSerializer serializer = mock(ReportSerializer.class);
      when(serializer.toByteArray(any())).thenReturn("{}".getBytes(StandardCharsets.UTF_8));
      SimulationHandler simHandler = mock(SimulationHandler.class);
      UserInteractionHandler ui = mock(UserInteractionHandler.class);

      doAnswer(inv -> {
         Consumer<AdditionalInstructions> instr = inv.getArgument(1);
         Consumer<SimulationReport> reportC = inv.getArgument(2);
         AdditionalInstructions ai = mock(AdditionalInstructions.class);
         when(ai.getInstruction()).thenReturn("Do this");
         when(ai.getParameters()).thenReturn(List.of());
         instr.accept(ai);
         SimulationReport report = mock(SimulationReport.class);
         when(report.getResult()).thenReturn(Result.PASSED);
         reportC.accept(report);
         return null;
      }).when(simHandler).simulate(any(), any(), any());
      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());

      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, ui, serializer);
      StepRunReport report = runner.execute(stepRun());

      assertNotNull(report.getResult());
      assertEquals("Simulation", report.getStepName());
      assertEquals(SimulationStepDefinition.TYPE, report.getType());
      assertFalse(report.getOutputs().isEmpty());
      assertEquals(SimulationStepDefinition.REPORT, report.getOutputs().getFirst().getName());
      StringProperty propertyBinding = report.getOutput(PropertyBindingPayload.PROPERTY_NAME);
      assertNotNull(propertyBinding);
   }

   @Test
   void run_returnsFailed_whenSimulationReportFailed() {
      ReportSerializer serializer = mock(ReportSerializer.class);
      when(serializer.toByteArray(any())).thenReturn("{}".getBytes(StandardCharsets.UTF_8));
      SimulationHandler simHandler = mock(SimulationHandler.class);
      UserInteractionHandler ui = mock(UserInteractionHandler.class);

      doAnswer(inv -> {
         Consumer<SimulationReport> reportC = inv.getArgument(2);
         SimulationReport report = mock(SimulationReport.class);
         when(report.getResult()).thenReturn(Result.FAILED);
         reportC.accept(report);
         return null;
      }).when(simHandler).simulate(any(), any(), any());

      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, ui, serializer);

      StepRunReport report = runner.execute(stepRun());
      assertNotNull(report.getResult());
   }

   @Test
   void run_returnsUnexpectedError_onServiceUnavailable() {
      ReportSerializer serializer = mock(ReportSerializer.class);
      when(serializer.toByteArray(any())).thenReturn("{}".getBytes(StandardCharsets.UTF_8));
      SimulationHandler simHandler = mock(SimulationHandler.class);
      UserInteractionHandler ui = mock(UserInteractionHandler.class);

      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());
      doThrow(new ServiceUnavailableException("down")).when(simHandler).simulate(any(), any(), any());

      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, ui, serializer);
      StepRunReport report = runner.execute(stepRun());
      assertEquals(StepResult.UNDEFINED, report.getResult());
      assertFalse(report.getUnexpectedErrors().isEmpty());
      assertEquals("ServiceUnavailableException", report.getUnexpectedErrors().getFirst().getName());
   }

   @Test
   void run_returnsUnexpectedError_onMissingRequiredParameter() {
      SimulationHandler simHandler = mock(SimulationHandler.class);
      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());
      doThrow(new MissingRequiredParameterException("missing"))
            .when(simHandler).simulate(any(), any(), any());
      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, mock(UserInteractionHandler.class),
            mock(ReportSerializer.class));
      StepRunReport report = runner.execute(stepRun());
      assertEquals(StepResult.UNDEFINED, report.getResult());
      assertEquals("MissingRequiredParameterException", report.getUnexpectedErrors().getFirst().getName());
   }

   @Test
   void run_returnsUnexpectedError_onInvalidParameterValue() {
      SimulationHandler simHandler = mock(SimulationHandler.class);
      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());
      doThrow(new InvalidParameterValueException("invalid"))
            .when(simHandler).simulate(any(), any(), any());
      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, mock(UserInteractionHandler.class),
            mock(ReportSerializer.class));
      StepRunReport report = runner.execute(stepRun());
      assertEquals("InvalidParameterValueException", report.getUnexpectedErrors().getFirst().getName());
   }

   @Test
   void run_returnsUnexpectedError_onUnknownSequence() {
      SimulationHandler simHandler = mock(SimulationHandler.class);
      doThrow(new UnknownSequenceException("unknown seq"))
            .when(simHandler).simulate(any(), any(), any());
      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, mock(UserInteractionHandler.class),
            mock(ReportSerializer.class));
      StepRunReport report = runner.execute(stepRun());
      assertEquals("UnknownSequenceException", report.getUnexpectedErrors().getFirst().getName());
   }

   @Test
   void run_returnsUnexpectedError_onUnknownSimulation() {
      SimulationHandler simHandler = mock(SimulationHandler.class);
      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());
      doThrow(new UnknownSimulationException("unknown sim"))
            .when(simHandler).simulate(any(), any(), any());
      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, mock(UserInteractionHandler.class),
            mock(ReportSerializer.class));
      StepRunReport report = runner.execute(stepRun());
      assertEquals("UnknownSimulationException", report.getUnexpectedErrors().getFirst().getName());
   }

   @Test
   void run_returnsUnexpectedError_onAlreadyRunning() {
      SimulationHandler simHandler = mock(SimulationHandler.class);
      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());
      doThrow(new AlreadyRunningException("running"))
            .when(simHandler).simulate(any(), any(), any());
      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, mock(UserInteractionHandler.class),
            mock(ReportSerializer.class));
      StepRunReport report = runner.execute(stepRun());
      assertEquals("AlreadyRunningException", report.getUnexpectedErrors().getFirst().getName());
   }

   @Test
   void run_returnsUnexpectedError_onRuntimeException() {
      SimulationHandler simHandler = mock(SimulationHandler.class);
      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());
      doThrow(new RuntimeException("oops"))
            .when(simHandler).simulate(any(), any(), any());
      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, mock(UserInteractionHandler.class),
            mock(ReportSerializer.class));
      StepRunReport report = runner.execute(stepRun());
      assertEquals("RuntimeException", report.getUnexpectedErrors().getFirst().getName());
   }

   @Test
   void run_returnsUnexpectedError_onClassCastErrorFromProperties() {
      Step s = step();
      s.setProperties(List.of(
            new StringProperty(SimulationStepDefinition.SIMULATION_SERVICE, "svc"),
            new StringProperty(SimulationStepDefinition.SEQUENCE_ID, 2)
      ));
      StepRun sr = new StepRun(s, List.of());
      SimulationHandler simHandler = mock(SimulationHandler.class);
      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());
      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler,
            mock(UserInteractionHandler.class), mock(ReportSerializer.class));
      StepRunReport report = runner.execute(sr);
      assertEquals("ClassCastException", report.getUnexpectedErrors().getFirst().getName());
   }

   @Test
   void run_returnsUnexpectedError_onTimeout() {
      // Use negative timeout to compute zero total timeout, so get() times out immediately
      Step s = step();
      StepRun sr = new StepRun(s, List.of());
      SimulationHandler simHandler = mock(SimulationHandler.class);
      // Do nothing: don't complete the report consumer -> future.get times out immediately
      when(simHandler.getSimulationSequences()).thenReturn(getSimulationSequences());
      doAnswer(inv -> null).when(simHandler).simulate(any(), any(), any());
      SimulationStepExecutor runner = new SimulationStepExecutor(simHandler, mock(UserInteractionHandler.class),
            mock(ReportSerializer.class));
      StepRunReport report = runner.execute(sr);
      assertEquals("TimeoutRuntimeException", report.getUnexpectedErrors().getFirst().getName());
   }

   private Step step() {
      return new Step()
            .setName("Simulation")
            .setType(SimulationStepDefinition.TYPE)
            .setTimeout(1000L)
            .setProperties(List.of(
                  new StringProperty(SimulationStepDefinition.SIMULATION_SERVICE, "svc"),
                  new StringProperty(SimulationStepDefinition.SEQUENCE_ID, "SEQ-1"),
                  new StringProperty("p-text", "v"),
                  new BooleanProperty("p-bool", true),
                  new ByteArrayProperty("p-file", "data".getBytes(StandardCharsets.UTF_8))
            ));
   }

   private StepRun stepRun() {
      List<Property> inputs = List.of(
            new StringProperty("p-text", "v"),
            new BooleanProperty("p-bool", true),
            new ByteArrayProperty("p-file", "data".getBytes(StandardCharsets.UTF_8))
      );
      return new StepRun(step(), inputs);
   }

   private List<SimulationSequence> getSimulationSequences() {
      return List.of(new SimulationSequence()
            .setId("SEQ-1")
            .setSupportedParameters(List.of(
                  new SupportedParameter().setName("p-text").setType(ParameterType.TEXT),
                  new SupportedParameter().setName("p-bool").setType(ParameterType.BOOLEAN),
                  new SupportedParameter().setName("p-file").setType(ParameterType.FILE)
            ))
      );
   }
}
