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

package net.ihe.gazelle.maestro.itb.step.business;

import net.ihe.gazelle.errorhandling.business.UnexpectedErrorBuilder;
import net.ihe.gazelle.itb.gateway.business.ItbExecutionMode;
import net.ihe.gazelle.itb.gateway.business.ItbSessionStore;
import net.ihe.gazelle.itb.gateway.business.RunItbTestService;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbResult;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInput;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInputMappingEntry;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStartRequest;
import net.ihe.gazelle.lang.ExecutionRuntimeException;
import net.ihe.gazelle.lang.TimeoutRuntimeException;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.SupportedInput;
import net.ihe.gazelle.maestro.api.business.testreport.StepResult;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReportBuilder;
import net.ihe.gazelle.maestro.spi.business.StepExecutor;
import net.ihe.gazelle.maestro.spi.business.StepRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Implementation of the StepExecutor interface for handling ITB step executions.
 */
public class ItbStepExecutor implements StepExecutor {

   private static final Logger LOG = LoggerFactory.getLogger(ItbStepExecutor.class);

   private static final Map<String, SupportedInput> SUPPORTED_INPUTS = new ItbStepDefinition().getSupportedInputs()
         .stream().collect(
               Collectors.toMap(SupportedInput::getId, input -> input)
         );
   private static final String SYSTEM_ID = "systemID";

   private final RunItbTestService itbService;

   /**
    * Constructs an instance of the {@code ItbStepExecutor} class, which is responsible for executing
    * ITB test steps by using the provided ITB handler and session store.
    *
    * @param itbHandler the handler interface for managing ITB services and test sessions.
    * @param sessionStore the session store that handles the storage and retrieval of ITB reporting sessions.
    */
   public ItbStepExecutor(ItbHandler itbHandler, ItbSessionStore sessionStore) {
      itbService = new RunItbTestService(itbHandler, sessionStore);
   }

   @Override
   public StepRunReport execute(StepRun stepRun) {
      try {
         ItbStartRequest testRequest = mapToRequest(stepRun);
         StepRunReportBuilder reportBuilder = new StepRunReportBuilder()
               .setStepName(stepRun.getName())
               .setType(ItbStepDefinition.TYPE);

         ItbExecutionMode executionMode = resolveExecutionMode(stepRun);
         CompletableFuture<ItbReporting> futureReport = itbService.runTests(testRequest, executionMode, stepRun.getTimeout());

         return waitItbReport(stepRun, futureReport, reportBuilder);
      } catch (RuntimeException e) {
         LOG.error("Error while executing ITB step '{}'", stepRun.getName(), e);
         return buildReportWithError(stepRun, e);
      }
   }

   private StepRunReport waitItbReport(StepRun stepRun, CompletableFuture<ItbReporting> futureReport, StepRunReportBuilder reportBuilder) {
      long timeout = stepRun.getTimeout();
      try {
         return futureReport
               .thenApply(itbReporting -> mapToStepRunReport(reportBuilder, itbReporting))
               .get(stepRun.getTimeout(), TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
         LOG.error("ITB timed out after {} ms", timeout);
         throw new TimeoutRuntimeException("ITB timed out after " + timeout + " ms");
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new ExecutionRuntimeException(e);
      } catch (ExecutionException e) {
         throw new ExecutionRuntimeException(e);
      }
   }

   private ItbExecutionMode resolveExecutionMode(StepRun stepRun) {
      if (!stepRun.hasProperty(ItbStepDefinition.EXECUTION_MODE)) {
         return ItbExecutionMode.ASYNC;
      }
      String value = stepRun.getPropertyValue(ItbStepDefinition.EXECUTION_MODE);
      if (value == null || value.isBlank()) {
         return ItbExecutionMode.ASYNC;
      }
      try {
         return ItbExecutionMode.valueOf(value.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(
               "Unsupported ITB executionMode '" + value + "'. Expected ASYNC or SYNC.", e
         );
      }
   }

   private ItbStartRequest mapToRequest(StepRun stepRun) {
      String testCaseId = stepRun.getPropertyValue(ItbStepDefinition.TEST_CASE);
      List<String> testCaseSelection = testCaseId == null ? null : List.of(testCaseId);
      ItbStartRequest testRequest = new ItbStartRequest()
            .setActor(stepRun.getPropertyValue(ItbStepDefinition.ACTOR_ID))
            .setTestCase(testCaseSelection);
      Set<String> sourceReferenceNames = collectSourceReferenceNames(stepRun);
      List<Property> propertiesToSend = collectPropertiesToSend(stepRun, testRequest, sourceReferenceNames);
      if (!propertiesToSend.isEmpty()) {
         testRequest.setInputMapping(buildInputMapping(stepRun, testCaseSelection, propertiesToSend));
      }
      return testRequest;
   }

   private Set<String> collectSourceReferenceNames(StepRun stepRun) {
      Set<String> sourceReferenceNames = new HashSet<>();
      for (Property property : stepRun.getProperties()) {
         String referenceName = stepRun.getPropertyReferenceName(property.getName());
         if (referenceName != null) {
            sourceReferenceNames.add(referenceName);
         }
      }
      return sourceReferenceNames;
   }

   private List<Property> collectPropertiesToSend(StepRun stepRun, ItbStartRequest testRequest, Set<String> sourceReferenceNames) {
      List<Property> propertiesToSend = new ArrayList<>();
      for (Property property : stepRun.getProperties()) {
         if (sourceReferenceNames.contains(property.getName()) && !SYSTEM_ID.equals(property.getName())
               && !SUPPORTED_INPUTS.containsKey(property.getName())) {
            continue;
         }
         if (!SUPPORTED_INPUTS.containsKey(property.getName())) {
            if (SYSTEM_ID.equals(property.getName())) {
               Object rawValue = property.getValue();
               testRequest.setSystem(rawValue == null ? null : rawValue.toString());
            } else {
               propertiesToSend.add(property);
            }
         }
      }
      return propertiesToSend;
   }

   private List<ItbInputMappingEntry> buildInputMapping(StepRun stepRun, List<String> testCaseSelection, List<Property> propertiesToSend) {
      ItbPayloadMode payloadMode = resolvePayloadMode(stepRun);
      if (payloadMode == ItbPayloadMode.USER_INPUT_MAP) {
         return List.of(new ItbInputMappingEntry()
               .setTestCase(testCaseSelection)
               .setInput(new ItbInput()
                     .setName("userInput")
                     .setType("map")
                     .setItem(propertiesToSend.stream().map(ItbStepExecutor::mapToUserInputItem).toList())));
      }
      return propertiesToSend.stream()
            .map(property -> new ItbInputMappingEntry()
                  .setTestCase(testCaseSelection)
                  .setInput(mapToInput(property)))
            .toList();
   }

   private StepRunReportBuilder prepareStepReport(StepRun stepRun) {
      return new StepRunReportBuilder()
            .setStepName(stepRun.getName())
            .setType(ItbStepDefinition.TYPE);
   }

   private StepRunReport buildReportWithError(StepRun stepRun, Exception e) {
      return prepareStepReport(stepRun)
            .addUnexpectedError(new UnexpectedErrorBuilder().fromThrowable(e))
            .build();
   }

   private static ItbInput mapToInput(Property property) {
      return switch (property) {
         case ByteArrayProperty baProp -> new ItbInput()
               .setName(baProp.getName())
               .setType("binary")
               .setEmbeddingMethod("BASE64")
               .setValue(Base64.getEncoder().encodeToString(baProp.getValue()));
         case StringProperty strProp -> new ItbInput()
               .setName(strProp.getName())
               .setType("string")
               .setValue(strProp.getValue());
         default -> new ItbInput()
               .setName(property.getName())
               .setType("string")
               .setValue(String.valueOf(property.getValue()));
      };
   }

   private static ItbInput mapToUserInputItem(Property property) {
      return switch (property) {
         case ByteArrayProperty baProp -> new ItbInput()
               .setName(baProp.getName())
               .setType("binary")
               .setEmbeddingMethod("BASE64")
               .setValue(Base64.getEncoder().encodeToString(baProp.getValue()));
         case StringProperty strProp -> new ItbInput()
               .setName(strProp.getName())
               .setValue(strProp.getValue());
         default -> new ItbInput()
               .setName(property.getName())
               .setValue(String.valueOf(property.getValue()));
      };
   }

   private ItbPayloadMode resolvePayloadMode(StepRun stepRun) {
      if (!stepRun.hasProperty(ItbStepDefinition.ITB_PAYLOAD_MODE)) {
         return ItbPayloadMode.FLAT;
      }
      String value = stepRun.getPropertyValue(ItbStepDefinition.ITB_PAYLOAD_MODE);
      if (value == null || value.isBlank()) {
         return ItbPayloadMode.FLAT;
      }
      try {
         return ItbPayloadMode.valueOf(value.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(
               "Unsupported ITB payload mode '" + value + "'. Expected FLAT or USER_INPUT_MAP.", e
         );
      }
   }


   private StepRunReport mapToStepRunReport(StepRunReportBuilder reportBuilder, ItbReporting itbReporting) {
      reportBuilder.setResult(asStepResult(itbReporting.getResult()));
      if (itbReporting.getTestReport() != null) {
         reportBuilder.addOutput(getItbXMLReport(itbReporting));
      }
      if (itbReporting.getPdfReport() != null) {
         reportBuilder.addOutput(getItbPdfReport(itbReporting));
      }
      if (itbReporting.getLogs() != null) {
         reportBuilder.addOutput(getLogs(itbReporting));
      }
      return reportBuilder.build();
   }

   private StepResult asStepResult(ItbResult itbResult) {
      return switch (itbResult) {
         case ItbResult.SUCCESS -> StepResult.PASSED;
         case ItbResult.FAILURE -> StepResult.FAILED;
         default -> StepResult.UNDEFINED;
      };
   }

   private Property getItbXMLReport(ItbReporting itbReporting) {
      return new ByteArrayProperty(ItbStepDefinition.XML_REPORT, itbReporting.getTestReport().getBytes())
            .setFileName("itb-report.xml")
            .setMimeType("application/xml");
   }

   private Property getItbPdfReport(ItbReporting itbReporting) {
      return new ByteArrayProperty(ItbStepDefinition.PDF_REPORT, itbReporting.getPdfReport())
            .setFileName("itb-report.pdf")
            .setMimeType("application/pdf");
   }

   private Property getLogs(ItbReporting itbReporting) {
      return new ByteArrayProperty(ItbStepDefinition.LOGS, itbReporting.getLogs().getBytes(StandardCharsets.UTF_8))
            .setFileName("itb-logs.txt")
            .setMimeType("text/plain");
   }

}
