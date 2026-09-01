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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.errorhandling.business.UnexpectedErrorBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.ObjectResult;
import net.ihe.gazelle.lang.ExecutionRuntimeException;
import net.ihe.gazelle.lang.TimeoutRuntimeException;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.PropertyBindingPayload;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReportBuilder;
import net.ihe.gazelle.maestro.api.business.userinteract.UserInteractionHandler;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import net.ihe.gazelle.simulation.business.callback.Result;
import net.ihe.gazelle.simulation.business.callback.SimulationReport;
import net.ihe.gazelle.simulation.business.callback.SimulationReportValidator;
import net.ihe.gazelle.simulation.business.sequence.SupportedParameter;
import net.ihe.gazelle.simulation.business.setup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Executor for simulation steps.
 * This class executes simulation sequences and processes simulation reports.
 */
public class SimulationStepExecutor implements StepExecutor {

   private static final Logger LOG = LoggerFactory.getLogger(SimulationStepExecutor.class);
   private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

   private final SimulationHandler simulationHandler;
   private final UserInteractionHandler userInteractionHandler;
   private final ReportSerializer serializer;
   private final SimulationReportValidator reportValidator = new SimulationReportValidator();

   /**
    * Constructor for SimulationStepExecutor.
    * @param simulationHandler the simulation handler to execute simulations
    * @param userInteractionHandler the handler for user interactions during simulation
    * @param serializer the serializer for converting simulation reports to byte arrays
    */
   public SimulationStepExecutor(SimulationHandler simulationHandler,
                                 UserInteractionHandler userInteractionHandler,
                                 ReportSerializer serializer) {
      this.simulationHandler = simulationHandler;
      this.userInteractionHandler = userInteractionHandler;
      this.serializer = serializer;
   }

   @Override
   public StepRunReport execute(StepRun stepRun) {
      try {
         String simulationService = stepRun.getPropertyValue(SimulationStepDefinition.SIMULATION_SERVICE);
         String sequenceId = stepRun.getPropertyValue(SimulationStepDefinition.SEQUENCE_ID);
         List<PropertyBindingPayload.Binding> propertyBindings = new ArrayList<>();

         SimulationRequest simulationRequest = createSimulationRequest(
               sequenceId,
               getTimeoutSeconds(stepRun),
               stepRun,
               getSupportedParameters(simulationService, sequenceId),
               propertyBindings
         );

         CompletableFuture<SimulationReport> futureReport = new CompletableFuture<>();
         simulationHandler.simulate(simulationRequest, new InstructionConsumer(stepRun.getTimeout()), futureReport::complete);
         SimulationReport simulationReport = waitSimulationReport(simulationRequest, futureReport);

         return buildStepRunReport(stepRun, simulationReport, propertyBindings);
      } catch (RuntimeException e) {
         return buildReportWithError(stepRun, e);
      }
   }

   private static int getTimeoutSeconds(StepRun stepRun) {
       long timeoutMillis = stepRun.getTimeout();
       if (timeoutMillis <= 0) {
           throw new InvalidParameterValueException("The timeout cannot be negative.");
       }
       long seconds = TimeUnit.MILLISECONDS.toSeconds(timeoutMillis);
       return seconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) seconds;
   }

   private List<SupportedParameter> getSupportedParameters(String simulationService, String sequenceId) {
      return simulationHandler.getSimulationSequences().stream()
            .filter(seq -> sequenceId.equals(seq.getId()))
            .findFirst().orElseThrow(() ->
                  new UnknownSequenceException(
                        "Unknown Simulation Sequence with id: " + sequenceId + " for service: " + simulationService))
            .getSupportedParameters();
   }

   /**
    * Creates a simulation request from step run parameters.
    * @param sequenceId the simulation sequence identifier
    * @param timeoutSeconds the timeout in seconds for the simulation
    * @param stepRun the step run containing the parameters
    * @param supportedParameters the list of supported parameters for this sequence
    * @param propertyBindings the list of property bindings
    * @return the configured simulation request
    */
   public static SimulationRequest createSimulationRequest(String sequenceId,
                                                           int timeoutSeconds,
                                                           StepRun stepRun,
                                                           List<SupportedParameter> supportedParameters,
                                                           List<PropertyBindingPayload.Binding> propertyBindings) {
      SimulationRequestBuilder simulationRequest = new SimulationRequestBuilder()
            .setSequenceId(sequenceId)
            .setTimeoutSeconds(timeoutSeconds);

      if (!supportedParameters.isEmpty()) {
         for (SupportedParameter supportedParameter : supportedParameters) {
            if (supportedParameter.isRequired()) {
               Property property = stepRun.getProperty(supportedParameter.getName());
               simulationRequest.addSimulationParameter(
                     toParameter(property, supportedParameter)
               );
               propertyBindings.add(toPropertyBinding(stepRun, property, supportedParameter.getName(), "simulationParameter"));
            } else {
               if (stepRun.hasProperty(supportedParameter.getName())) {
                  Property property = stepRun.getProperty(supportedParameter.getName());
                  simulationRequest.addSimulationParameter(
                        toParameter(property, supportedParameter)
                  );
                  propertyBindings.add(toPropertyBinding(stepRun, property, supportedParameter.getName(), "simulationParameter"));
               }
            }
         }
      }

      return simulationRequest.build();
   }

   private static ParameterBuilder toParameter(final Property property, SupportedParameter supportedParameter) {
      return new ParameterBuilder()
            .setName(supportedParameter.getName())
            .setType(supportedParameter.getType())
            .setValue(toParameterValue(
                  property, supportedParameter.getType()
            ));
   }

   private static String toParameterValue(Property property, ParameterType expectedType) {
      return switch (expectedType) {
         case TEXT -> (String) property.getValue();
         case BOOLEAN -> ((Boolean) property.getValue()).toString();
         case FILE -> Base64.getEncoder().encodeToString((byte[]) property.getValue());
      };
   }

   private SimulationReport waitSimulationReport(SimulationRequest simulationRequest,
                                                 CompletableFuture<SimulationReport> futureReport) {
      long timeout = simulationRequest.getTimeoutSeconds() + 3L;
      try {
         return futureReport.get(timeout, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
         LOG.error("Simulation timed out after {} seconds", timeout);
         throw new TimeoutRuntimeException("Simulation timed out after " + timeout + " seconds");
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
          throw new ExecutionRuntimeException(e);
      } catch (ExecutionException e) {
         throw new ExecutionRuntimeException(e);
      }
   }

   private StepRunReport buildStepRunReport(StepRun stepRun,
                                            SimulationReport simulationReport,
                                            List<PropertyBindingPayload.Binding> propertyBindings) {

      ObjectResult reportValidationResult = reportValidator.validate(simulationReport);
      StringProperty propertyBindingOutput = new StringProperty(
            PropertyBindingPayload.PROPERTY_NAME,
            serializeBindings(new PropertyBindingPayload()
                  .setStepType(SimulationStepDefinition.TYPE)
                  .setBindings(propertyBindings))
      );
      return prepareStepReport(stepRun)
            .setResult(toStepResult(simulationReport.getResult()))
            .setOutputs(List.of(
                  new ByteArrayProperty(SimulationStepDefinition.REPORT, serializer.toByteArray(simulationReport))
                        .setFileName("simulation-report.json")
                        .setMimeType("application/json"),
                  propertyBindingOutput
            ))
            .addUnexpectedErrors(
                  reportValidationResult.getAllInvalidRules().stream()
                        .map(ruleResult -> new UnexpectedErrorBuilder()
                              .setName("Invalid simulation report (" + ruleResult.getId() + ")")
                              .setMessage(ruleResult.getDescription())
                        ).toList()
            )
            .build();
   }

   private StepRunReportBuilder prepareStepReport(StepRun stepRun) {
      return new StepRunReportBuilder()
            .setStepName(stepRun.getName())
            .setType(SimulationStepDefinition.TYPE);
   }

   private StepRunReport buildReportWithError(StepRun stepRun, Exception e) {
      return prepareStepReport(stepRun)
            .addUnexpectedError(new UnexpectedErrorBuilder().fromThrowable(e))
            .build();
   }

   private StepResult toStepResult(Result result) {
      return switch (result) {
         case PASSED -> StepResult.PASSED;
         case FAILED -> StepResult.FAILED;
         default -> StepResult.UNDEFINED;
      };
   }

   private static PropertyBindingPayload.Binding toPropertyBinding(StepRun stepRun,
                                                                   Property property,
                                                                   String targetId,
                                                                   String targetKind) {
      PropertyBindingPayload.Binding binding = new PropertyBindingPayload.Binding()
            .setTargetKind(targetKind)
            .setTargetId(targetId)
            .setReferenceName(resolveReferenceName(stepRun, property))
            .setPropertyName(property.getName());
      if (property instanceof ByteArrayProperty byteArrayProperty) {
         binding.setFileName(byteArrayProperty.getFileName());
         binding.setMimeType(byteArrayProperty.getMimeType());
      }
      return binding;
   }

   private static String resolveReferenceName(StepRun stepRun, Property property) {
      String referenceName = stepRun.getPropertyReferenceName(property.getName());
      return referenceName != null ? referenceName : property.getReferenceName();
   }

   private static String serializeBindings(PropertyBindingPayload payload) {
      try {
         return OBJECT_MAPPER.writeValueAsString(payload);
      } catch (JsonProcessingException e) {
         throw new IllegalStateException("Failed to serialize property bindings", e);
      }
   }

    private class InstructionConsumer implements Consumer<AdditionalInstructions> {

        private final long timeout;

        public InstructionConsumer(long timeout) {
            this.timeout = timeout;
        }

        @Override
        public void accept(AdditionalInstructions additionalInstructions) {
            StringBuilder instructions = new StringBuilder()
                    .append(additionalInstructions.getInstruction())
                    .append("\n");
            for (Parameter parameter : additionalInstructions.getParameters()) {
                instructions
                        .append("name : ").append(parameter.getName()).append("\n")
                        .append("value : ").append(parameter.getValue()).append("\n");
            }
            userInteractionHandler.displayMessage("Execution paused", instructions.toString(), timeout);
        }
    }
}
